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

        return dbUser;
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
        return passwordAuth.get().getUser();
    }

    public void createVerificationToken(User user, String token){
        VerificationToken myToken = new VerificationToken(token, user);
        verificationTokenRepository.save(myToken);
    }

    public VerificationToken getVerificationToken(String token){
        return verificationTokenRepository.findByToken(token);
    }

    public boolean confirmUser(String token){
        VerificationToken myToken = verificationTokenRepository.findByToken(token);
        if(myToken == null){
            throw new UserDbAuthenticationException("No such token user to verify");
        }
        User user = myToken.getUser();
        Calendar cal = Calendar.getInstance();
        if ((myToken.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0){
            throw new UserDbAuthenticationException("Verification link outdated");
        }
        user.setEnabled(true);
        userRepository.save(user);
        return true;
    }


    public void logOut(Long userId){
        Optional<User> user = userRepository.findById(userId);
        user.get().setCounter(user.get().getCounter()+1);
        userRepository.save(user.get());
    }

    public String passwordReset(User user){
        String token = UUID.randomUUID().toString();
        user.setEnabled(false);
        user.setCounter(user.getCounter()+1);
        verificationTokenRepository.save(new VerificationToken(token, user));
        userRepository.save(user);
        return token;
    }

    public Optional<User> getById(Long userid){
        return userRepository.findById(userid);
    }

    public String getEmailById(Long userid){
        return passwordAuthRepository.findById(userid).get().getEmail();
    }

    public User getByEmail(String email){
        return passwordAuthRepository.getByEmail(email).get().getUser();
    }
}
