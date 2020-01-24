package eu.coinform.gateway.db;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import javax.security.auth.message.AuthException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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

    public User registerUser(String email, String password, List<RoleEnum> roleList) throws UsernameAlreadyExistException {
        if (passwordAuthRepository.existsByEmail(email)) {
            throw new UsernameAlreadyExistException(email);
        }

        User user = new User();
        User dbUser =  userRepository.save(user);
        dbUser.setRoles(roleList.stream().map(roleEnum -> new Role(dbUser.getId(), dbUser, roleEnum)).collect(Collectors.toList()));
        PasswordAuth passwordAuth = new PasswordAuth();
        passwordAuth.setEmail(email.toLowerCase());
        passwordAuth.setPassword(passwordEncoder.encode(password));
        passwordAuth.setUser(dbUser);
        passwordAuth.setId(dbUser.getId());
        dbUser.setPasswordAuth(passwordAuth);
        userRepository.save(dbUser);

        return user;
    }

    public User logIn(String email, String password) throws AuthenticationException {
        Optional<PasswordAuth> passwordAuth = passwordAuthRepository.getByEmail(email.toLowerCase());
        if (passwordAuth.isEmpty()) {
            throw new UsernameNotFoundException("No such username");
        }

        if (!passwordEncoder.matches(password, passwordAuth.get().getPassword())) {
            throw new BadCredentialsException("Incorrect Password");
        }
        return passwordAuth.get().getUser();
    }
}
