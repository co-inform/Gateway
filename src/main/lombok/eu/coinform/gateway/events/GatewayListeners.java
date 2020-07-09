package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.entity.ModuleInfo;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.db.entity.VerificationToken;
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
import java.util.Date;
import java.util.Optional;

@Slf4j
@Component
public class GatewayListeners {

    private final EmailService emailService;
    private final UserDbManager userDbManager;

    @Value("${claimcredibility.server.scheme}://${claimcredibility.server.url}${claimcredibility.server.base_endpoint}/user/accuracy-review")
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
            sendToModule(mapper.writeValueAsString(event.getSource()), claimCredHost, userInfo);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @Async("endpointExecutor")
    @EventListener
    public void userTweetEvaluationListener(UserTweetEvaluationEvent event){
        try {
            String result = sendToModule(mapper.writeValueAsString(event.getSource()), claimCredHost, userInfo);
            log.info("CLAIM REVIEW: {}", result);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @Async("endpointExecutor")
    @EventListener
    public void failedModuleRequestListener(FailedModuleRequestEvent event){
        log.debug("Sending email to {} owner about failed request", event.getModule());
        ModuleInfo moduleInfo = userDbManager.findByModulename(event.getModule()).get();
        Date now = new Date();
        long threshold = 1000*60*60*24L;
        if(now.getTime() - moduleInfo.getFailtime().getTime() > threshold){
            log.info("More than 24 hours since last failed request. Sending email to module owner");
            emailService.sendFailedModuleRequestEmail(moduleInfo.getUser().getPasswordAuth().getEmail(),moduleInfo.getModulename(),event.getMessage(),now);
            moduleInfo.setFailtime(now);
            userDbManager.saveModuleInfo(moduleInfo);
        }
    }


    // Methods below are evaluations sent to external partners

    @Async("endpointExecutor")
    @EventListener
    public void userTweetEvaluationListener(SendToSomaEvent event){
        //todo: THis could be changed to catch an event returned from one of the above method instead.
        // Will investigate once soma integration is worked on.
        try {
            String result = sendToModule(mapper.writeValueAsString(event.getSource()), String.format(somaUrl,collectionId), somaJWT);
            log.info("SOMA: {}", result);
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        }

    }

    // Method below is the generic method to send evaluations to modules/partners

    private String sendToModule(String body, String url, String auth){
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
            return status.body();
        } catch (InterruptedException | IOException e) {
            log.debug("HTTP error: {}", e.getMessage());
        }
        return "";
    }

}
