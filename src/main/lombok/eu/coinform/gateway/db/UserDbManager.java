package eu.coinform.gateway.db;

import com.google.common.collect.Lists;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserDbManager { //implements ApplicationListener<OnRegistrationCompleteEvent> {

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

    public User updateUser(User user){
        User u = userRepository.save(user);
        return u;
    }

    public User logIn(String email, String password) throws AuthenticationException, UserNotVerifiedException {
        Optional<PasswordAuth> passwordAuth = passwordAuthRepository.getByEmail(email.toLowerCase());
        if (passwordAuth.isEmpty()) {
            throw new UsernameNotFoundException("No such username");
        }

        if (!passwordEncoder.matches(password, passwordAuth.get().getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }

//        passwordAuth.get().getUser().isEnabled();
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

    public User getUser(VerificationToken verificationToken){
        return userRepository.findByVerificationToken(verificationToken);
    }
}
