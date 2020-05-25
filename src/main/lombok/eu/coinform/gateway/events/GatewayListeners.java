package eu.coinform.gateway.events;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.restclient.RestClient;
import eu.coinform.gateway.db.UserDbManager;
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

    @Value("${soma.url")
    protected String somaUrl;

    @Value("${soma.jwt")
    protected String somaJWT;

    protected ObjectMapper mapper = new ObjectMapper();

    GatewayListeners(EmailService emailService, UserDbManager userDbManager){
        this.emailService = emailService;
        this.userDbManager = userDbManager;
    }

    @Async
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

    @Async
    @EventListener
    public void passwordChangeListener(PasswordChangeEvent event) {
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }

    @Async
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

    @Async
    @EventListener
    public void successfulPasswordResetListener(SuccessfulPasswordResetEvent event){
        User user = event.getUser();
        emailService.sendSuccessMessage(user.getPasswordAuth().getEmail());
    }


    // Methods below are evaluations sent of to ClaimCred module

    @Async
    @EventListener
    public void userLabelReviewListener(UserLabelReviewEvent event){
        try {
            sendToClaimCred(mapper.writeValueAsString(event.getSource()));
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    @Async
    @EventListener
    public void userTweetEvaluationListener(UserTweetEvaluationEvent event){
        try {
            sendToClaimCred(mapper.writeValueAsString(event.getSource()));
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}",e.getMessage());
        }
    }

    private void sendToClaimCred(String body){
        HttpResponse<String> status;

        try {
            RestClient client = new RestClient(HttpMethod.POST,
                    URI.create(claimCredHost),
                            body,
                            "Authorization", userInfo);
            status = client.sendRequest().join();
            if(status.statusCode() < 200 || status.statusCode() > 299){
                log.debug("RestClient status: {}", status);
            }
        } catch (InterruptedException | IOException e) {
            log.debug("HTTP error: {}", e.getMessage());
        }
    }

    @Async
    @EventListener
    public void userTweetEvaluationListener(SendToSomaEvent event){
        HttpResponse<String> status;

        try {
            RestClient client = new RestClient(HttpMethod.POST,
                    URI.create(somaUrl),
                    mapper.writeValueAsString(event.getSource().getValue()),
                    "Authorization", somaJWT);
            status = client.sendRequest().join();
            if(status.statusCode() < 200 || status.statusCode() > 299){
                log.debug("RestClient status: {}", status);
            }
        } catch (JsonProcessingException e) {
            log.debug("JSON error: {}", e.getMessage());
        } catch (InterruptedException | IOException e) {
            log.debug("HTTP error: {}", e.getMessage());
        }

    }


}
