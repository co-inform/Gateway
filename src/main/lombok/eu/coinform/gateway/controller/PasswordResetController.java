package eu.coinform.gateway.controller;

import eu.coinform.gateway.db.User;
import eu.coinform.gateway.db.UserDbAuthenticationException;
import eu.coinform.gateway.db.UserDbManager;
import eu.coinform.gateway.db.VerificationToken;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@Controller
@Slf4j
public class PasswordResetController {

    private final UserDbManager userDbManager;

    PasswordResetController(UserDbManager userDbManager){
        this.userDbManager = userDbManager;
    }

    @RequestMapping(value = "/passwordreset", method = RequestMethod.GET)
    public String passwordReset(@RequestParam(name = "token", required = true) String token, Model model){
        log.debug("Param: {}", token);
        VerificationToken myToken = userDbManager.getVerificationToken(token);
        User user = myToken.getUser();
        log.debug("myToken: {}", myToken.getToken());
        if (user == null) {
            throw new UserDbAuthenticationException("No such token exist");
        }

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

        VerificationToken token = userDbManager.getVerificationToken(form.token);
        User user = token.getUser();

        if(user == null){
            log.debug("User null");
            throw new UsernameNotFoundException("No such user");
        }

        if(!userDbManager.newPassword(user, form.pw1)){
            throw new UserDbAuthenticationException("Oups");
        }



        return "success";
    }
}
