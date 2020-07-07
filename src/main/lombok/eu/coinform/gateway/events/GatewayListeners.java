package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.forms.RecordRequestForm;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.entity.VerificationToken;
import eu.coinform.gateway.module.iface.FactChecker;
import eu.coinform.gateway.module.iface.ItemToReview;
import eu.coinform.gateway.service.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.event.EventListener;
import org.springframework.http.HttpMethod;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class GatewayListeners {

    private final EmailService emailService;
    private final UserDbManager userDbManager;

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

    GatewayListeners(EmailService emailService, UserDbManager userDbManager){
        this.emailService = emailService;
        this.userDbManager = userDbManager;
    }

    @Async("endpointExecutor")
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

    @Async("endpointExecutor")
    @EventListener
    public void passwordChangeListener(PasswordChangeEvent event) {
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }

    @Async("endpointExecutor")
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

    @Async("endpointExecutor")
    @EventListener
    public void successfulPasswordResetListener(SuccessfulPasswordResetEvent event){
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }


    // Methods below are evaluations sent of to ClaimCred module

    @Async("endpointExecutor")
    @EventListener
    public void userLabelReviewListener(UserLabelReviewEvent event){
        try {
            HttpResponse<String> result = sendToModule(mapper.writeValueAsString(event.getSource()), claimCredHost+"/user/accuracy-review", userInfo);
            log.info("REVIEW: {}", result);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @Async("endpointExecutor")
    @EventListener
    public void userTweetEvaluationListener(UserTweetEvaluationEvent event){
        try {
            HttpResponse<String> result = sendToModule(mapper.writeValueAsString(event.getSource()), claimCredHost+"/user/accuracy-review", userInfo);
            log.info("CLAIM REVIEW: {}", result);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @Async("endpointExecutor")
    @EventListener
    public void externalReviewReceivedListener(ExternalReviewReceivedEvent event){
        try {
            HttpResponse<String> result = sendToModule(mapper.writeValueAsString(event.getSource()), claimCredHost+"/factchecker/review", userInfo);
            log.info("EXTERNAL REVIEW: {}", result);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }
    }


    // Methods below are evaluations sent to external partners

    @Async("endpointExecutor")
    @EventListener
    public void userTweetEvaluationListener(SendToSomaEvent event){
        //todo: THis could be changed to catch an event returned from one of the above method instead.
        // Will investigate once soma integration is worked on.
        try {
            event.getSource().setCollectionId(collectionId);
            HttpResponse<String> result = sendToModule(mapper.writeValueAsString(event.getSource()), String.format(somaUrl,collectionId), somaJWT);
            log.info("SOMA: {}", result);
            if(result != null && (result.statusCode() >= 200 && result.statusCode() <= 299)){
                List<FactChecker> fcList = List.of(new FactChecker("Organization","Truly-Media","http://truly.media"));
                ItemToReview itReview = new ItemToReview("SocialMediaPost", event.getSource().getValue());
                RecordRequestForm rrform = new RecordRequestForm(fcList, itReview);
                HttpResponse<String> ccresult = sendToModule(mapper.writeValueAsString(rrform), claimCredHost+"/factchecker/recordRequest", userInfo);
                log.info("CC RESULT: {}", ccresult);
            }
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }

    }

    // Method below is the generic method to send evaluations to modules/partners

    private HttpResponse<String> sendToModule(String body, String url, String auth){
        HttpResponse<String> status;

        try {
            RestClient client = new RestClient(HttpMethod.POST,
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
