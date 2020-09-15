package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.controller.forms.*;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.entity.ModuleInfo;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.entity.VerificationToken;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.iface.ClaimCredAction;
import eu.coinform.gateway.module.iface.FactChecker;
import eu.coinform.gateway.module.iface.ItemToReview;
import eu.coinform.gateway.service.EmailService;
import eu.coinform.gateway.service.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
public class GatewayListeners {

    private final EmailService emailService;
    private final UserDbManager userDbManager;
    private final RedisHandler redisHandler;

    @Value("${claimcredibility.server.scheme}://${claimcredibility.server.url}${claimcredibility.server.base_endpoint}")
    protected String claimCredHost;

    @Value("${CLAIM_CRED_USER_INFO}")
    protected String userInfo;

    @Value("${gateway.scheme}://${gateway.url}")
    protected String gatewayUrl;

    @Value("${soma.url}")
    protected String somaUrl;

    @Value("${soma.jwt}")
    protected String somaJWT;

    @Value("${soma.collectionid}")
    protected String collectionId;

    protected ObjectMapper mapper = new ObjectMapper();

    GatewayListeners(EmailService emailService, UserDbManager userDbManager, RedisHandler redisHandler){
        this.emailService = emailService;
        this.userDbManager = userDbManager;
        this.redisHandler = redisHandler;
    }

    @EventListener
    public void passwordResetListener(OnPasswordResetEvent event){
        User user = event.getUser();
        String token = userDbManager.resetPassword(user);
        if(token.isEmpty()){
            log.debug("Something went wrong when fetching token for {}", user. getPasswordAuth().getEmail());
            return;
        }
        String verifyUrl = gatewayUrl + "/passwordreset?token="+token;
        emailService.sendPasswordResetMessage(user.getPasswordAuth().getEmail(), verifyUrl);
    }

    @EventListener
    public void passwordChangeListener(PasswordChangeEvent event) {
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }

    @EventListener
    public void registrationCompleteListener(OnRegistrationCompleteEvent event){
        User user = event.getUser();
        Optional<VerificationToken> oToken = userDbManager.getVerificationToken(user);
        if(oToken.isEmpty()){
            log.debug("No verificationToken for user {}", user.getPasswordAuth().getEmail());
            return;
        }
        String toAddress = user.getPasswordAuth().getEmail();
        String verifyUrl = gatewayUrl + "/registrationConfirm?token=" + oToken.get().getToken();
        emailService.sendVerifyEmailMessage(toAddress,verifyUrl);
    }

    @EventListener
    public void successfulPasswordResetListener(SuccessfulPasswordResetEvent event){
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }


    // Methods below are evaluations sent of to ClaimCred module

    @EventListener
    public void userLabelReviewListener(UserLabelReviewEvent event){
        try {
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), claimCredHost+"/user/accuracy-review", userInfo);
            log.info("LABEL REVIEW: {}", result.body());
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @EventListener
    public void userTweetEvaluationListener(UserTweetEvaluationEvent event){
        try {
            String url = claimCredHost + "/user/accuracy-review"; //?factCheckRequested=" + event.getForm().isRequestFactcheck();
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), url, userInfo);
            log.info("CLAIM REVIEW: {}", result.body());
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @EventListener
    public void externalReviewReceivedListener(ExternalReviewReceivedEvent event){
        try {
            // The review from EXTERNAL partner has arrived, store it with our backend
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), claimCredHost+"/factchecker/review", userInfo);
            //todo: Here we should receive a list of uuids connected to users who has requested a review for this tweet.
            // logic for emailing them a link to the review needs to be implemented.
            log.info("EXTERNAL REVIEW: {}", result.body());
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }
    }

    @EventListener
    public void feedBackReviewListener(FeedbackReviewEvent event){
        log.info("Requesting userFeedbacks from ESI");
        Tweet tweet = (Tweet) event.getQueryObject();
        HttpResponse<String> res = sendToModule(HttpMethod.GET, "", String.format(claimCredHost+"/tweet/accuracy-review?tweet_id=%s", tweet.getTweetId()), userInfo);
        if(res == null){
            return;
        }

        try {
            boolean updatedCache;
            do {
                UserFeedback response = mapper.readValue(res.body(), UserFeedback.class);
                QueryResponse qr = redisHandler.getQueryResponse(tweet.getQueryId()).join();
                long oldVersionHash = qr.getVersionHash();
                qr.setVersionHash();
                qr.setAgreementFeedback(response.getResponse().getCredibilityReviews().getAgreementFeedback());
                updatedCache = redisHandler.setQueryResponseAtomic(tweet.getQueryId(), qr, oldVersionHash).join();
            } while (!updatedCache);
        } catch (JsonProcessingException e) {
            log.debug("JSONPROCESSING exception: {}",res.body());
            e.printStackTrace();
        }
    }

    @EventListener
    public void failedModuleRequestListener(FailedModuleRequestEvent event){
        log.info("Sending email to {} owner about failed request", event.getModule());
        Optional<ModuleInfo> oModuleInfo = userDbManager.findByModulename(event.getModule());
        if(oModuleInfo.isEmpty()){
            log.error("No ModuleInfo found for {}", event.getModule());
            return;
        }
        ModuleInfo moduleInfo = oModuleInfo.get();
        Date now = new Date();
        long threshold = 1000*60*60*24L;
        if(moduleInfo.getFailtime() == null || now.getTime() - moduleInfo.getFailtime().getTime() > threshold){
            log.info("More than 24 hours since last failed request. Sending email to module owner");
            emailService.sendFailedModuleRequestEmail(moduleInfo.getUser().getPasswordAuth().getEmail(),moduleInfo.getModulename(),event.getMessage(),now);
            moduleInfo.setFailtime(now);
            userDbManager.saveModuleInfo(moduleInfo);
        }
    }


    // Methods below are evaluations sent to external partners

    @EventListener
    public void sendToSomaEventListener(SendToSomaEvent event){

        if(!event.isRequestFactcheck()){
            log.info("No factcheck requested by user");
            return;
        }

        try {
            event.getSource().setCollectionId(collectionId);
            // Send the tweet of to SOMA for external review
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), String.format(somaUrl,collectionId), somaJWT);
            log.info("SOMA: {}", result != null ? result.body() : null);
            if(result != null){
                List<FactChecker> fcList = List.of(new FactChecker("Organization","Truly-Media","http://truly.media"));
                ItemToReview itReview = new ItemToReview("SocialMediaPosting", event.getSource().getValue());
                RecordRequestForm rrform = new RecordRequestForm(fcList, itReview);
                log.debug("RRFORM: {}", rrform);
                // Store the request of a review with SOMA with our backend
                HttpResponse<String> ccresult = sendToModule(HttpMethod.POST, mapper.writeValueAsString(rrform), claimCredHost+"/factchecker/recordRequest", userInfo);
                log.info("CC RESULT: {}", ccresult != null ? ccresult.body() : null);
            }
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }

    }

    @EventListener
    private void evaluationLogReceivedListener(EvaluationLogReceivedEvent event) {
        List<ClaimCredAction> pluginActions = event.getEvaluationLogList().stream()
                .map(pel -> new ClaimCredAction(pel, event.sessionToken))
                .collect(Collectors.toList());
        String body;
        try {
            body = mapper.writeValueAsString(pluginActions);
            log.trace("plugin action list: {}", body);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
            return;
        }
        sendToModule(HttpMethod.POST, body, claimCredHost + "/log/plugin/action", userInfo);
    }

    // Method below is the generic method to send evaluations to modules/partners

    private HttpResponse<String> sendToModule(HttpMethod method, String body, String url, String auth){
        HttpResponse<String> status;
        try {
            RestClient client = new RestClient(method,
                    URI.create(url),
                    body,
                    "Authorization", auth);
            status = client.sendRequest().join();
            if(status.statusCode() < 200 || status.statusCode() > 299){
                log.info("RestClient status: {}", status);
                log.info("Body: {}", body);
                log.info("Url: {}", url);
                log.info("Auth: {}", auth);
            }
            return status;
        } catch (InterruptedException | IOException e) {
            log.debug("HTTP error: {}", e.getMessage());
        }
        return null;
    }

}
