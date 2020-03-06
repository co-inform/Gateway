package eu.coinform.gateway.controller;

import eu.coinform.gateway.controller.forms.PasswordChangeForm;
import eu.coinform.gateway.controller.forms.PasswordResetForm;
import eu.coinform.gateway.controller.forms.RegisterForm;
import eu.coinform.gateway.db.*;
import eu.coinform.gateway.db.entity.RoleEnum;
import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.entity.User;
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

import javax.servlet.http.*;
import javax.validation.Valid;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class UserController {

    private final String RENEWAL_TOKEN_NAME = "renew-token";
    private final int RENEWAL_TOKEN_MAXAGE = 60*60*24*90;
    private final String RENEWAL_TOKEN_DOMAIN = "coinform.eu";

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

    @RequestMapping(value = "/renew-token", method = RequestMethod.GET)
    public ResponseEntity<?> renewToken(HttpServletRequest request) {
        //todo: First ever initialization, return 404 when no renew-token supplied
        Optional<Cookie> cookie = findCookie(RENEWAL_TOKEN_NAME,request);
        if(cookie.isEmpty()){
            return ResponseEntity.notFound().build();
        }
        String token = cookie.get().getValue();
        Optional<SessionToken> ost = userDbManager.getSessionTokenByToken(token);

        if(ost.isEmpty()) {
           return ResponseEntity.notFound().build();
        }

        User user = ost.get().getUser();
        log.debug("User: {} ", user.getPasswordAuth().getEmail());

        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        String jwtToken = (new JwtToken.Builder())
                .setSignatureAlgorithm(SignatureAlgorithm.HS512)
                .setKey(signatureKey)
                .setCounter(user.getCounter())
                .setExpirationTime(10000L) //todo: set to 2*60*1000L when done testing this.
                .setUser(user.getId())
                .setRoles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build().getToken();

        // todo: JWT token renewal,
        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }

    private Optional<Cookie> findCookie(String key, HttpServletRequest request){
        return Arrays.stream(request.getCookies())
                .filter(cookie -> key.equals(cookie.getName()))
                .findAny();
    }

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login(HttpServletResponse response) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        Long userId;

        if(authentication instanceof JwtAuthenticationToken){
            userId = (Long) authentication.getPrincipal();
        } else {
            userId = userDbManager.getByEmail(authentication.getName()).getId();
        }

        Optional<User> user = userDbManager.getById(userId);

        if(user.isEmpty()){
            throw new UserDbAuthenticationException("User not found");
        }

        String token = (new JwtToken.Builder())
                .setSignatureAlgorithm(SignatureAlgorithm.HS512)
                .setKey(signatureKey)
                .setCounter(user.get().getCounter())
                .setExpirationTime(10000L) //todo: set to 2*60*1000L when doen testing this.
                .setUser(userId)
                .setRoles(authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build().getToken();

        SessionToken st = new SessionToken(user.get());
        userDbManager.saveSessionToken(st);
        final Cookie cookie = new Cookie(RENEWAL_TOKEN_NAME, st.getSessionToken());
        cookie.setDomain(RENEWAL_TOKEN_DOMAIN);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(RENEWAL_TOKEN_MAXAGE);
        response.addCookie(cookie);
        return new LoginResponse(token);
    }

    @RequestMapping(value = "/register", method = RequestMethod.POST)
    public ResponseEntity<?> register(@RequestBody @Valid RegisterForm registerForm) throws UsernameAlreadyExistException {

        List<RoleEnum> roles = new LinkedList<>();
        roles.add(RoleEnum.USER);

        User user = userDbManager.registerUser(registerForm.getEmail(), registerForm.getPassword(), roles);

        try {
            eventPublisher.publishEvent(new OnRegistrationCompleteEvent(user));
        } catch (Exception me){
            log.debug(me.getMessage());
            ResponseEntity.badRequest().body(ErrorResponse.USEREXISTS);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(SuccesfullResponse.USERCREATED);
    }



    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetForm form) {
        User user = userDbManager.getByEmail(form.getEmail());
        if(user == null) {
            return ResponseEntity.badRequest().body(ErrorResponse.NOUSER);
        }

        try{
            eventPublisher.publishEvent(new OnPasswordResetEvent(user));
        } catch (Exception e){
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().body(ErrorResponse.NOUSER);
        }

        return ResponseEntity.ok(SuccesfullResponse.PASSWORDRESET);
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ResponseEntity<?> passwordChange(@RequestBody @Valid PasswordChangeForm form){

        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authentication = ctx.getAuthentication();

        Long userid = (Long) authentication.getPrincipal();

        if(!userDbManager.passwordChange(userid, form.getNewPassword(), form.getOldPassword())){
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
    public ResponseEntity<?> logout(HttpServletResponse response){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        userDbManager.logOut((Long) authentication.getPrincipal());
        final Cookie cookie = new Cookie(RENEWAL_TOKEN_NAME, "");
        cookie.setDomain(RENEWAL_TOKEN_DOMAIN);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok(SuccesfullResponse.USERLOGGEDOUT);
    }


}
