package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.controller.forms.*;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.entity.ModuleInfo;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.entity.VerificationToken;
import eu.coinform.gateway.model.Tweet;
import eu.coinform.gateway.module.iface.*;
import eu.coinform.gateway.service.EmailService;
import eu.coinform.gateway.service.RedisHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
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
            log.warn("Something went wrong when fetching token for {}", user. getPasswordAuth().getEmail());
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
            log.warn("No verificationToken for user {}", user.getPasswordAuth().getEmail());
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
            log.info("LABEL REVIEW: {}", result != null ? result.statusCode() : null);
            log.debug("LABEL REVIEW: {}", result != null ? result.body() : null);
        } catch (JsonProcessingException e) {
            log.error("JSON error: {}",e.getMessage());
        }
    }

    @EventListener
    public void userTweetEvaluationListener(UserTweetEvaluationEvent event){
        try {
            String url = claimCredHost + "/user/accuracy-review"; //?factCheckRequested=" + event.getForm().isRequestFactcheck();
            log.debug("tweet evaluation sent to claim credibility: {}", mapper.writeValueAsString(event.getSource()));
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), url, userInfo);
            log.info("CLAIM REVIEW: {}", result != null ? result.statusCode() : null);
            log.debug("CLAIM REVIEW: {}", result != null ? result.body() : null);
        } catch (JsonProcessingException e) {
            log.error("JSON error: {}",e.getMessage());
        }
    }

    @EventListener
    public void externalReviewReceivedListener(ExternalReviewReceivedEvent event){
        try {
            // The review from EXTERNAL partner has arrived, store it with our backend
            HttpResponse<String> result = sendToModule(HttpMethod.POST, mapper.writeValueAsString(event.getSource()), claimCredHost+"/factchecker/review", userInfo);
            List<FeedbackRequester> feedbackRequesters = new LinkedList<>();
            JsonNode root = mapper.readTree(result.body());
            root.get("response")
                .get("relatedUserReviews")
                .get("usersWhoRequestedFactcheck")
                .elements().forEachRemaining(node -> {
                    JsonNode ar = node.get("mostRecentAccuracyReview");
                    feedbackRequesters.add(new FeedbackRequester(
                            node.get("author.url").asText(),
                            node.get("type").asText(),
                            new AccuracyReview(
                                    ar.get("name").asText(),
                                    new ReviewRating(ar.get("reviewRating").get("ratingValue").asText()),
                                    ZonedDateTime.parse(ar.get("dateCreated").asText(),
                                            DateTimeFormatter.ISO_DATE_TIME),
                                    ar.get("text").asText())));
            });

            feedbackRequesters.forEach((fr) -> {
                userDbManager.getUserByUUID(fr.getAuthorUUID()).ifPresentOrElse(
                        (user) -> emailService.sendUserRequestedFactcheckFeedback(user.getPasswordAuth().getEmail(), fr, event.getExternalEvaluationForm()),
                        () -> log.error("User UUID don't exist, ${}", fr.getAuthorUUID())
                );
            });
            log.info("EXTERNAL REVIEW: {}", result != null ? result.statusCode() : null);
            log.debug("EXTERNAL REVIEW: {}", result != null ? result.body() : null);
        } catch (JsonProcessingException e) {
            log.error("JSON error: {}", e.getMessage());
        }
    }

    @EventListener
    public void feedBackReviewListener(FeedbackReviewEvent event){
        log.info("Requesting userFeedbacks from ESI");
        Tweet tweet = (Tweet) event.getQueryObject();
        String url;
        if(tweet.getUserId() == null || tweet.getUserId().isEmpty() || !userDbManager.existsByUuid(tweet.getUserId())){
            url = String.format(claimCredHost+"/tweet/accuracy-review?tweet_id=%s", tweet.getTweetId());
        } else {
            url = String.format(claimCredHost+"/tweet/accuracy-review?tweet_id=%s&user_id=%s", tweet.getTweetId(),tweet.getUserId());
        }
        log.debug("Feedback URL: {}", url);
        HttpResponse<String> res = sendToModule(HttpMethod.GET, "", url, userInfo);
        if(res == null || res.body() == null){
            log.info("ESI Result null");
            return;
        }
        log.debug("User Feedback returned");
        if(tweet.getUserId() != null && !tweet.getUserId().isEmpty() && userDbManager.existsByUuid(tweet.getUserId())) {
            try {
                UserFeedback response = mapper.readValue(res.body(), UserFeedback.class);
                redisHandler.setDisagreementFeedback(tweet.getQueryId(), tweet.getUserId(), response.getResponse().getCredibilityReviews().getAgreementFeedback());
            } catch (JsonProcessingException e) {
                log.error("JSONPROCESSING exception: {}", res.body());
                e.printStackTrace();
            }
        }
        try {
            UserFeedback response = mapper.readValue(res.body(), UserFeedback.class);
            boolean updatedCache;
            do {
                QueryResponse qr = redisHandler.getQueryResponse(tweet.getQueryId()).join();
                long oldVersionHash = qr.getVersionHash();
                qr.setVersionHash();
                if (qr.getResponse() == null) {
                    qr.setResponse(new LinkedHashMap<>());
                }
                AgreementFeedback agreementFeedback = response.getResponse().getCredibilityReviews().getAgreementFeedback();
                agreementFeedback.getCredibilityUncertain().setUserFeedback(null);
                agreementFeedback.getCredibilityUncertain().setUserReviews(new LinkedList<>());
                agreementFeedback.getCredible().setUserFeedback(null);
                agreementFeedback.getCredible().setUserReviews(new LinkedList<>());
                agreementFeedback.getMostlyCredible().setUserFeedback(null);
                agreementFeedback.getMostlyCredible().setUserReviews(new LinkedList<>());
                agreementFeedback.getNotCredible().setUserFeedback(null);
                agreementFeedback.getNotCredible().setUserReviews(new LinkedList<>());
                agreementFeedback.getNotVerifiable().setUserFeedback(null);
                agreementFeedback.getNotVerifiable().setUserReviews(new LinkedList<>());

                qr.getResponse().put("(dis)agreement_feedback", agreementFeedback);
                updatedCache = redisHandler.setQueryResponseAtomic(tweet.getQueryId(), qr, oldVersionHash).join();
            } while (!updatedCache);
            log.debug("updatedCache successful");
        } catch (JsonProcessingException e) {
            log.error("JSONPROCESSING exception: {}", res.body());
            e.printStackTrace();
        }
    }

    @EventListener
    public void failedModuleRequestListener(FailedModuleRequestEvent event){
        Optional<ModuleInfo> oModuleInfo = userDbManager.findByModulename(event.getModule());
        if(oModuleInfo.isEmpty()){
            log.warn("No ModuleInfo found for {}", event.getModule());
            return;
        }
        ModuleInfo moduleInfo = oModuleInfo.get();
        Date now = new Date();
        long threshold = 1000*60*60*24L;
        if(moduleInfo.getFailtime() == null || now.getTime() - moduleInfo.getFailtime().getTime() > threshold){
            log.info("More than 24 hours since last failed request. Sending email to module owner");
            log.info("Sending email to {} owner about failed request", event.getModule());
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
            log.info("SOMA: {}", result != null ? result.statusCode() : null);
            log.debug("SOMA: {}", result != null ? result.body() : null);
            if(result != null){
                List<FactChecker> fcList = List.of(new FactChecker("Organization","Truly-Media","http://truly.media"));
                ItemToReview itReview = new ItemToReview("SocialMediaPosting", event.getSource().getValue());
                RecordRequestForm rrform = new RecordRequestForm(fcList, itReview);
                log.debug("RRFORM: {}", rrform);
                // Store the request of a review with SOMA with our backend
                HttpResponse<String> ccresult = sendToModule(HttpMethod.POST, mapper.writeValueAsString(rrform), claimCredHost+"/factchecker/recordRequest", userInfo);
                log.info("CC RESULT: {}", ccresult != null ? ccresult.statusCode() : null);
                log.debug("CC RESULT: {}", ccresult != null ? ccresult.body() : null);
            }
        } catch (JsonProcessingException e) {
            log.error("JSON error: {}", e.getMessage());
        }

    }

    @EventListener
    public void evaluationLogReceivedListener(EvaluationLogReceivedEvent event) {
        List<ClaimCredAction> pluginActions = event.getEvaluationLogList().stream()
                .map(pel -> new ClaimCredAction(pel, event.sessionToken))
                .collect(Collectors.toList());
        String body;
        try {
            body = mapper.writeValueAsString(pluginActions);
            log.trace("plugin action list: {}", body);
        } catch (JsonProcessingException e) {
            log.error("JSON error: {}", e.getMessage());
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
                log.debug("RestClient body: {}", status.body());
                log.info("Body: {}", body);
                log.info("Url: {}", url);
                log.info("Auth: {}", auth);
            }
            return status;
        } catch (InterruptedException | IOException e) {
            log.error("HTTP error: {}", e.getMessage());
        }
        return null;
    }

}
