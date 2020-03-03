package eu.coinform.gateway.db;

import com.google.common.collect.Lists;
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

    public UserDbManager(
            UserRepository userRepository,
            PasswordAuthRepository passwordAuthRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordAuthRepository = passwordAuthRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
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
        verificationTokenRepository.save(new VerificationToken(UUID.randomUUID().toString(), user));

        return dbUser;
    }

    public boolean newPassword(User user, String password) {
        user.getPasswordAuth().setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        return verificationTokenRepository.findByUser(user).map(token -> {
            verificationTokenRepository.delete(token);
            return true;
        }).or(() -> Optional.of(false)).get();
    }

    public User logIn(String email, String password) throws AuthenticationException, UserNotVerifiedException {
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

    public void createVerificationToken(User user, String token){
        log.debug("User: {}, token: {}", user, token);
        VerificationToken myToken = new VerificationToken(token, user);
        log.debug("myToken: {}, userId: {}", myToken.getToken(), myToken.getId());
        VerificationToken t = verificationTokenRepository.save(myToken);
        log.debug("t: {}, t.id: {}", t.getToken(), t.getId());
    }

    public Optional<VerificationToken> getVerificationToken(String token){
        return verificationTokenRepository.findByToken(token);
    }

    public boolean confirmUser(String token){
        Optional<VerificationToken> myToken = verificationTokenRepository.findByToken(token);
        if(myToken.isEmpty()){
            throw new UserDbAuthenticationException("No such token to verify");
        }
        User user = myToken.map(VerificationToken::getUser).get();
        Calendar cal = Calendar.getInstance();
        if ((myToken.get().getExpiryDate().getTime() - cal.getTime().getTime()) <= 0){
            throw new UserDbAuthenticationException("Verification link outdated");
        }
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(myToken.get());
        return true;
    }


    public void logOut(Long userId){
        Optional<User> user = userRepository.findById(userId);
        user.ifPresent(u -> u.setCounter(u.getCounter()+1)); // to invalidate the JWT token
        userRepository.save(user.get());
    }

    public String passwordReset(User user){
        log.debug("Resetting user: {}", user.getPasswordAuth().getEmail());
        user.setCounter(user.getCounter()+1); // to invalidate the JWT token
        Optional<VerificationToken> token = verificationTokenRepository.findByUser(user);
        String uuid = UUID.randomUUID().toString();

        token.ifPresent(verificationToken -> verificationTokenRepository.delete(verificationToken));
        verificationTokenRepository.save(new VerificationToken(uuid, user));
        userRepository.save(user);
        return uuid;
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

}
