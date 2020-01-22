package eu.coinform.gateway.db;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.message.AuthException;

@Service
public class UserDbManager {

    private UserRepository userRepository;
    private PasswordAuthRepository passwordAuthRepository;
    private PasswordEncoder passwordEncoder;

    UserDbManager(
            UserRepository userRepository,
            PasswordAuthRepository passwordAuthRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordAuthRepository = passwordAuthRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(String email, String password) throws UsernameAlreadyExistException {
        if (passwordAuthRepository.existsByEmail(email)) {
            throw new UsernameAlreadyExistException(email);
        }

        User user = new User();
        User dbUser =  userRepository.save(user);
        PasswordAuth passwordAuth = new PasswordAuth();
        passwordAuth.setEmail(email);
        passwordAuth.setPassword(passwordEncoder.encode(password));
        passwordAuth.setUser(dbUser);
        passwordAuth.setId(dbUser.getId());
        passwordAuthRepository.save(passwordAuth);

        return user;
    }

    public User logIn(String email, String password) throws AuthException {
        PasswordAuth passwordAuth = passwordAuthRepository.getByEmail(email);
        if (!passwordEncoder.matches(password, passwordAuth.getPassword())) {
            throw new AuthException("Incorrect Password");
        }
        return passwordAuth.getUser();
    }
}
