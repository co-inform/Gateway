package eu.coinform.gateway.controller;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.events.SuccessfulPasswordResetEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@Controller
@Slf4j
public class UserPagesController {

    private final UserDbManager userDbManager;
    private final ApplicationEventPublisher eventPublisher;

    UserPagesController(UserDbManager userDbManager,
                        ApplicationEventPublisher eventpublisher){
        this.userDbManager = userDbManager;
        this.eventPublisher = eventpublisher;
    }

    @RequestMapping(value = "/passwordreset", method = RequestMethod.GET)
    public String passwordReset(@RequestParam(name = "token", required = true) String token, Model model){
        Optional<VerificationToken> myToken = userDbManager.getVerificationToken(token);
        if(myToken.isEmpty()){
            throw new NoSuchTokenException("");
        }
        User user = myToken.get().getUser();

        model.addAttribute("userid", user.getPasswordAuth().getEmail());
        model.addAttribute("token", token);

        return "reset";
    }

    @PostMapping(value = "/resetting")
    public String saveNewPassword(@ModelAttribute NewPasswordForm form) {

        log.debug("Form values: {}, {}, {}", form.pw1, form.pw2, form.token);
        if(!form.pw1.equals(form.pw2)){
            return "mismatch";
        }

        VerificationToken token = userDbManager.getVerificationToken(form.token).map(t -> t).get();
        User user = token.getUser();

        if(user == null){
            log.debug("User null");
            throw new UsernameNotFoundException("No such user");
        }

        if(!userDbManager.passwordReset(user, form.pw1)){
            throw new UserDbAuthenticationException("Oups");
        }

        try {
            eventPublisher.publishEvent(new SuccessfulPasswordResetEvent(user));
        } catch (Exception e) {
            log.debug("Maybe not successful? {}", e.getMessage());
        }

        return "success";
    }

    @RequestMapping(value = "/registrationConfirm", method = RequestMethod.GET)
    public String confirmRegistration(@RequestParam("token") String token, Model model) throws LinkTimedOutException {


        Optional<VerificationToken> myToken = userDbManager.getVerificationToken(token);

        if(!userDbManager.confirmUser(token)){
            model.addAttribute("token", token);
            return "notverified";
        }

        model.addAttribute("userid", myToken.get().getUser().getPasswordAuth().getEmail());
        return "verified";
    }
}
