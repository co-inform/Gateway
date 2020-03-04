package eu.coinform.gateway.controller;

import eu.coinform.gateway.db.*;
import eu.coinform.gateway.events.OnPasswordResetEvent;
import eu.coinform.gateway.events.OnRegistrationCompleteEvent;
import eu.coinform.gateway.events.SuccessfulPasswordResetEvent;
import eu.coinform.gateway.jwt.JwtAuthenticationToken;
import eu.coinform.gateway.jwt.JwtToken;
import eu.coinform.gateway.util.ErrorResponse;
import eu.coinform.gateway.util.SuccesfullResponse;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class UserController {

    private final UserDbManager userDbManager;
    private final ApplicationEventPublisher eventPublisher;
    private final String signatureKey;

    UserController(UserDbManager userDbManager,
                   ApplicationEventPublisher eventPublisher,
                   @Value("${JWT_KEY}") String signatureKey) {
        this.userDbManager = userDbManager;
        this.eventPublisher = eventPublisher;
        this.signatureKey = signatureKey;
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login() {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        Long userId;

        if(authentication instanceof JwtAuthenticationToken){
            userId = (Long) authentication.getPrincipal();
        } else {
            userId = userDbManager.getByEmail(authentication.getName()).getId();
        }

        String token = (new JwtToken.Builder())
                .setSignatureAlgorithm(SignatureAlgorithm.HS512)
                .setKey(signatureKey)
                .setCounter(userDbManager.getById(userId).get().getCounter())
                .setExpirationTime(7*24*60*60*1000L)
                .setUser(userId)
                .setRoles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build().getToken();
        return new LoginResponse(token);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody @Valid RegisterForm registerForm) throws UsernameAlreadyExistException {

        List<RoleEnum> roles = new LinkedList<>();
        roles.add(RoleEnum.USER);

        User user = userDbManager.registerUser(registerForm.email, registerForm.password, roles);

        try {
            log.debug("Publishing event");
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
        } catch (Exception me){
            ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.USEREXISTS);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccesfullResponse.USERCREATED);
    }



    @RequestMapping(value = "/passwordreset", method = RequestMethod.POST)
    public ResponseEntity<?> passwordReset(@RequestBody @Valid PasswordResetForm form) {
        log.debug("Form: {}", form.email);
        User user = userDbManager.getByEmail(form.getEmail());
        if(user == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.NOUSER);
        }

        try{
            eventPublisher.publishEvent(new OnPasswordResetEvent(user));
        } catch (Exception e){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ErrorResponse.NOUSER);
        }

        return ResponseEntity.ok(SuccesfullResponse.PASSWORDRESET);
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ResponseEntity<?> passwordChange(@RequestBody @Valid PasswordChangeForm form){

        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authentication = ctx.getAuthentication();

        Long userid = (Long) authentication.getPrincipal();

        if(!userDbManager.passwordChange(userid, form.newPassword, form.oldPassword)){
            log.debug("oldPassword {}, newPassword {}", form.oldPassword, form.newPassword);
            throw new UserDbAuthenticationException("User has given a mismatching password");
        }

        try {
            User user = userDbManager.getById(userid).get();
            eventPublisher.publishEvent(new SuccessfulPasswordResetEvent(user));
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return ResponseEntity.ok(SuccesfullResponse.PASSWORDCHANGE);
    }

    @RequestMapping(value = "/exit", method = RequestMethod.GET)
    public ResponseEntity<?> logout(){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        userDbManager.logOut((Long) authentication.getPrincipal());
        return ResponseEntity.ok(SuccesfullResponse.USERLOGGEDOUT);
    }


}
