package eu.coinform.gateway.controller;

import eu.coinform.gateway.cache.QueryResponse;
import eu.coinform.gateway.controller.exceptions.MissingRenewToken;
import eu.coinform.gateway.controller.exceptions.NoSuchRenewToken;
import eu.coinform.gateway.controller.forms.PasswordChangeForm;
import eu.coinform.gateway.controller.forms.PasswordResetForm;
import eu.coinform.gateway.controller.forms.PluginEvaluationLog;
import eu.coinform.gateway.controller.forms.RegisterForm;
import eu.coinform.gateway.db.*;
import eu.coinform.gateway.db.entity.Role;
import eu.coinform.gateway.db.entity.RoleEnum;
import eu.coinform.gateway.db.entity.SessionToken;
import eu.coinform.gateway.db.entity.User;
import eu.coinform.gateway.events.OnPasswordResetEvent;
import eu.coinform.gateway.events.OnRegistrationCompleteEvent;
import eu.coinform.gateway.events.SuccessfulPasswordResetEvent;
import eu.coinform.gateway.jwt.JwtToken;
import eu.coinform.gateway.model.TwitterUser;
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
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.*;
import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@Slf4j
public class UserController {

    private final String RENEWAL_TOKEN_NAME = "renew-token";
    private final int RENEWAL_TOKEN_MAXAGE = 60*60*24*90;
    private final int RENEWAL_TOKEN_MAXAGE_MODULE = 60*60*24*365*50;
    @Value("${gateway.renewaltoken.domain}")
    private String RENEWAL_TOKEN_DOMAIN;
    @Value("${gateway.renewaltoken.secure:true}")
    private boolean secureCookie;

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

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/renew-token", method = RequestMethod.GET)
    public ResponseEntity<?> renewToken(HttpServletRequest request,
                                        @RequestParam(required = false, name = "plugin_version") String pluginVersion ) {
        Optional<Cookie> cookie = findCookie(RENEWAL_TOKEN_NAME,request);
        if(cookie.isEmpty()){
            throw new MissingRenewToken();
        }
        String token = cookie.get().getValue();
        Optional<SessionToken> ost = userDbManager.getSessionTokenByToken(token);

        if(ost.isEmpty()) {
           throw new NoSuchRenewToken();
        }

        User user = ost.get().getUser();
        ost.get().setCounter(ost.get().getCounter()+1);
        if (pluginVersion != null) {
            ost.get().setPluginVersion(pluginVersion);
        }
        userDbManager.saveUser(user);

        Collection<GrantedAuthority> grantedAuthorities = new LinkedList<>();
        for (Role role: user.getRoles()) {
            GrantedAuthority authority = new SimpleGrantedAuthority(role.getRole().toString());
            grantedAuthorities.add(authority);
        }

        String jwtToken = jwtTokenCreator(ost.get(), grantedAuthorities);

        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }

    @RequestMapping(value = "/renew-token", method = RequestMethod.OPTIONS)
    public void corsHeadersRenew(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/login", method = RequestMethod.POST)
    public LoginResponse login(HttpServletResponse response,
                               @RequestParam(required = false, name = "plugin_version") String pluginVersion ) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        User user = userDbManager.getByEmail(authentication.getName());

        if(user == null){
            throw new UserDbAuthenticationException("User not found");
        }

        SessionToken st;
        if(user.getSessionTokenList() == null) {
            user.setSessionTokenList(new LinkedList<>());
        }
        st = new SessionToken(user);
        if (pluginVersion != null) {
            st.setPluginVersion(pluginVersion);
        }
        userDbManager.saveSessionToken(st);
        String token = jwtTokenCreator(st, new ArrayList<GrantedAuthority>(authentication.getAuthorities()));

        final Cookie cookie = new Cookie(RENEWAL_TOKEN_NAME, st.getSessionToken());
        cookie.setDomain(RENEWAL_TOKEN_DOMAIN);
        cookie.setHttpOnly(true);
        if (authentication.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleEnum.MODULE.name()::equals)) {
            cookie.setMaxAge(RENEWAL_TOKEN_MAXAGE_MODULE);
        } else {
            cookie.setMaxAge(RENEWAL_TOKEN_MAXAGE);
        }
        cookie.setPath("/renew-token");
        cookie.setSecure(secureCookie);
        response.addCookie(cookie);
        return new LoginResponse(token);
    }

    @RequestMapping(value = "/login", method = RequestMethod.OPTIONS)
    public void corsHeadersLogin(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    private Optional<Cookie> findCookie(String key, HttpServletRequest request){
        if(request.getCookies() == null){
            return Optional.empty();
        }
        return Arrays.stream(request.getCookies())
                .filter(cookie -> key.equals(cookie.getName()))
                .findAny();
    }

    private String jwtTokenCreator(SessionToken st, Collection<GrantedAuthority> authorities){
        long expirationTime;
        if (authorities.stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(RoleEnum.MODULE.name()::equals)) {
            expirationTime = RENEWAL_TOKEN_MAXAGE_MODULE;
        } else {
            expirationTime = 2*60*60L;
        }
        return (new JwtToken.Builder())
                .setSignatureAlgorithm(SignatureAlgorithm.HS512)
                .setKey(signatureKey)
                .setExpirationTime(expirationTime)
                .setSessionToken(st)
                .setRoles(authorities.stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()))
                .build().getToken();
    }

    @CrossOrigin(origins = "*")
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

    @RequestMapping(value = "/register", method = RequestMethod.OPTIONS)
    public void corsHeadersRegister(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }


    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/reset-password", method = RequestMethod.POST)
    public ResponseEntity<?> resetPassword(@RequestBody @Valid PasswordResetForm form) {
        User user = userDbManager.getByEmail(form.getEmail());
        if(user == null) {
            log.debug("user null");
            return ResponseEntity.badRequest().body(ErrorResponse.NOSUCHUSER);
        }

        try{
            eventPublisher.publishEvent(new OnPasswordResetEvent(user));
        } catch (Exception e){
            log.debug(e.getMessage());
            return ResponseEntity.badRequest().body(ErrorResponse.NOSUCHUSER);
        }

        return ResponseEntity.ok(SuccesfullResponse.PASSWORDRESET);
    }

    @RequestMapping(value = "/reset-password", method = RequestMethod.OPTIONS)
    public void corsHeadersResetPassword(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/change-password", method = RequestMethod.POST)
    public ResponseEntity<?> passwordChange(@RequestBody @Valid PasswordChangeForm form){

        SecurityContext ctx = SecurityContextHolder.getContext();
        Authentication authentication = ctx.getAuthentication();

        Long sessionTokenId = (Long) authentication.getPrincipal();

        if(!userDbManager.passwordChange(sessionTokenId, form.getNewPassword(), form.getOldPassword())){
            throw new UserDbAuthenticationException("User has given a mismatching password");
        }

        User user = userDbManager.getBySessionTokenId(sessionTokenId).get();
        SessionToken st = user.getSessionTokenList().get(0);
        String jwtToken = jwtTokenCreator(st, new ArrayList<GrantedAuthority>(authentication.getAuthorities()));

        try {
            eventPublisher.publishEvent(new SuccessfulPasswordResetEvent(user));
        } catch (Exception e) {
            log.debug(e.getMessage());
        }

        return ResponseEntity.ok(new LoginResponse(jwtToken));
    }

    @RequestMapping(value = "/change-password", method = RequestMethod.OPTIONS)
    public void corsHeadersChangePassword(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/exit", method = RequestMethod.GET)
    public ResponseEntity<?> logout(HttpServletResponse response){
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();
        log.debug("logout principal {}", authentication.getPrincipal());
        SessionToken sessionToken = userDbManager.logOut((Long) authentication.getPrincipal()).get();
        final Cookie cookie = new Cookie(RENEWAL_TOKEN_NAME, sessionToken.getSessionToken());
        cookie.setDomain(RENEWAL_TOKEN_DOMAIN);
        cookie.setHttpOnly(true);
        cookie.setMaxAge(0);
        cookie.setPath("/renew-token");
        if (secureCookie) {
            cookie.setSecure(true);
        }
        response.addCookie(cookie);
        return ResponseEntity.ok(SuccesfullResponse.USERLOGGEDOUT);
    }

    @RequestMapping(value = "/exit", method = RequestMethod.OPTIONS)
    public void corsHeadersExit(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }

    @CrossOrigin(origins = "*")
    @RequestMapping(value = "/user/evaluation-log", method = RequestMethod.POST)
    public ResponseEntity<?> evaluationLog(HttpServletResponse response, @RequestBody @Valid List<PluginEvaluationLog> pluginEvaluationLogs) {
        SecurityContext context = SecurityContextHolder.getContext();
        Authentication authentication = context.getAuthentication();

        SessionToken st = userDbManager.getSessionToken((Long) authentication.getPrincipal()).get();
        log.debug("We recieved the following evaluation-logs from {} on plugin_version {}:",
                st.getUser().getUuid(), st.getPluginVersion());
        pluginEvaluationLogs.forEach((pel) -> log.debug("\t{}", pel));

        return ResponseEntity.ok(SuccesfullResponse.EVALUATIONLOGRECIEVED);
    }

    @RequestMapping(value = "/user/evaluation-log", method = RequestMethod.OPTIONS)
    public void corsHeadersEvaluationLog(HttpServletResponse response) {
        response.addHeader("Access-Control-Allow-Origin", "*");
        response.addHeader("Access-Control-Allow-Methods", "POST, OPTIONS");
        response.addHeader("Access-Control-Allow-Headers", "origin, content-type, accept, x-requested-with");
        response.addHeader("Access-Control-Max-Age", "3600");
    }
}
