package eu.coinform.gateway.db;

import com.google.common.collect.Lists;
import eu.coinform.gateway.db.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class UserDbManager {

    private UserRepository userRepository;
    private PasswordAuthRepository passwordAuthRepository;
    private RoleRepository roleRepository;
    private PasswordEncoder passwordEncoder;
    private VerificationTokenRepository verificationTokenRepository;
    private SessionTokenRepository sessionTokenRepository;

    public UserDbManager(
            UserRepository userRepository,
            PasswordAuthRepository passwordAuthRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            SessionTokenRepository sessionTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordAuthRepository = passwordAuthRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    public User registerUser(String email, String password, List<RoleEnum> roleList) throws UsernameAlreadyExistException {
        if (passwordAuthRepository.existsByEmail(email)) {
            throw new UsernameAlreadyExistException(email);
        }

        User user = new User();
        user.setCreatedAt(new Date());
        User dbUser = userRepository.save(user);
        PasswordAuth passwordAuth = new PasswordAuth();
        passwordAuth.setEmail(email.toLowerCase());
        passwordAuth.setPassword(passwordEncoder.encode(password));
        passwordAuth.setUser(dbUser);
        passwordAuth.setId(dbUser.getId());
        dbUser.setPasswordAuth(passwordAuth);
        dbUser.setRoles(
                Lists.newLinkedList(
                        roleRepository.saveAll(
                                roleList.stream().map(role -> new Role(dbUser.getId(), dbUser, role)).collect(Collectors.toList()))));

        userRepository.save(dbUser);
        verificationTokenRepository.save(new VerificationToken(user));

        return dbUser;
    }


    public boolean passwordChange(Long userid, String newPassword, String oldPassword){
        User user = userRepository.findById(userid).get();
        if(!passwordEncoder.matches(oldPassword, user.getPasswordAuth().getPassword())){
            return false;
        }
        user.getPasswordAuth().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    public User logIn(String email, String password) throws AuthenticationException {
        Optional<PasswordAuth> passwordAuth = passwordAuthRepository.getByEmail(email.toLowerCase());
        if (passwordAuth.isEmpty()) {
            throw new UsernameNotFoundException("No such username");
        }

        if (!passwordEncoder.matches(password, passwordAuth.get().getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }

        if(!userRepository.findById(passwordAuth.get().getId()).get().isEnabled()){
            throw new UserNotVerifiedException("User not verified");
        }

        verificationTokenRepository.findByUser(passwordAuth.get().getUser()).ifPresent(token -> {
            verificationTokenRepository.delete(token);
        });

        return passwordAuth.get().getUser();
    }

    public VerificationToken getAndDeleteVerificationToken(String token){
        Optional<VerificationToken> oToken = verificationTokenRepository.findByToken(token);

        if(oToken.isPresent()){
            verificationTokenRepository.delete(oToken.get());
            return oToken.get();
        }
        return null;
    }

    public Optional<VerificationToken> getVerificationToken(String token){
        return verificationTokenRepository.findByToken(token);
    }

    public Optional<VerificationToken> getVerificationToken(User user){
        return verificationTokenRepository.findByUser(user);
    }

    public boolean confirmUser(String token) throws LinkTimedOutException{
        Optional<VerificationToken> oToken = verificationTokenRepository.findByToken(token);
        if(oToken.isEmpty()){
            throw new NoSuchTokenException(token);
        }
        VerificationToken myToken = oToken.get();
        if (myToken.checkExpiryDatePassed(new Date())) {
            throw new LinkTimedOutException("Verification link timed out");
        }
        User user = myToken.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }


    public void logOut(Long userId){
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> {
            u.setCounter(u.getCounter()+1);
            sessionTokenRepository.deleteById(userId);
            userRepository.save(u);
        }); // to invalidate the JWT token and remove longlived session
    }

    public boolean changePassword(User user, String password) {
        user.getPasswordAuth().setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        Optional<VerificationToken> oToken = verificationTokenRepository.findByUser(user);
        oToken.ifPresent(token -> verificationTokenRepository.delete(token));
        return true;
    }

    public String resetPassword(User user){
        log.debug("Resetting user: {}", user.getPasswordAuth().getEmail());
        Optional<VerificationToken> oToken = verificationTokenRepository.findByUser(user);
        String token = "";

        if(oToken.isEmpty()){
            //no token. create one, store it and send to the user
            VerificationToken myToken = new VerificationToken(user);
            verificationTokenRepository.save(myToken);
            token = myToken.getToken();
        } else if(oToken.get().checkExpiryDatePassed(new Date())) {
            //there is a token but it has expired. create a new one, store it and send it
            verificationTokenRepository.delete(oToken.get());
            VerificationToken myToken = new VerificationToken(user);
            verificationTokenRepository.save(myToken);
            token = myToken.getToken();
        } else if(!oToken.get().checkExpiryDatePassed(new Date())) {
            //there is a valid token. send that to the user, dont change it
            token = oToken.get().getToken();
        }

        return token;
    }

    public Optional<User> getById(Long userid){
        return userRepository.findById(userid);
    }

    public String getEmailById(Long userid){
        return passwordAuthRepository.findById(userid)
                .map(PasswordAuth::getEmail).get();
    }

    public User getByEmail(String email){
        return passwordAuthRepository.getByEmail(email)
                .map(PasswordAuth::getUser).get();
    }

    public SessionToken getSessionTokenByUser(User user) {
        return sessionTokenRepository.findByUser(user).get();
    }

    public SessionToken saveSessionToken(SessionToken token){
        return sessionTokenRepository.save(token);
    }

    public Optional<SessionToken> getSessionTokenByToken(String token){
        return sessionTokenRepository.findBySessionToken(token);
    }

}
