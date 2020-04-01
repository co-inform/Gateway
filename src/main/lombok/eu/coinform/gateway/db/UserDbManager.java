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

    /**
     * Registers a new User with the CoInform system using the supplied email, password and list of roles
     *
     * @param email the users email
     * @param password the users password
     * @param roleList a list of actual roles for the user
     * @return the created User
     * @throws UsernameAlreadyExistException
     */

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

    /**
     * Change password for the user belonging to the passed userid.
     * @param userid Long holding the userid
     * @param newPassword the new password for the user
     * @param oldPassword the old password that is stored in the database
     * @return true if password successfully changed, otherwise false
     */

    public boolean passwordChange(Long userid, String newPassword, String oldPassword){
        User user = userRepository.findById(userid).get();
        if(!passwordEncoder.matches(oldPassword, user.getPasswordAuth().getPassword())){
            return false;
        }
        user.getPasswordAuth().setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
        return true;
    }

    /**
     * logIn(String, String) logs a user in to the CoInform system
     * @param email the email for the User
     * @param password the password for the User
     * @return the logged in User if succesfull
     * @throws AuthenticationException if not possible to authenticate the user
     */

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

    /**
     * Finds and returns the VerificationToken that equals the supplied String. Also deletes it from the database.
     * @param token the String to search for in the database
     * @return the found VerificationToken or null
     */

    public VerificationToken getAndDeleteVerificationToken(String token){
        Optional<VerificationToken> oToken = verificationTokenRepository.findByToken(token);

        if(oToken.isPresent()){
            verificationTokenRepository.delete(oToken.get());
            return oToken.get();
        }
        return null;
    }

    /**
     * Retreives an Optional holding the VerificationToken found for the supplied String
     * @param token the String to search for
     * @return an Option holding the VerificatationToken
     */

    public Optional<VerificationToken> getVerificationToken(String token){
        return verificationTokenRepository.findByToken(token);
    }

    /**
     * Retreives an Optional holding the Verification connected to the supplied User
     * @param user the User whom to search for a VerificationToken
     * @return returns an Optional holding the VerificationToken
     */
    public Optional<VerificationToken> getVerificationToken(User user){
        return verificationTokenRepository.findByUser(user);
    }

    public void confirmUser(VerificationToken token) throws LinkTimedOutException{

        Calendar cal = Calendar.getInstance();
        if ((token.getExpiryDate().getTime() - cal.getTime().getTime()) <= 0){
            throw new LinkTimedOutException("Verification link timed out");
        }
        User user = token.getUser();
        user.setEnabled(true);
        userRepository.save(user);
        verificationTokenRepository.delete(token);
    }

    /**
     * Logs a user out of the CoInform system. Invalidates both the JWT token and the SessionToken
     * @param sessionTokenId the userId for the specific user whom to logout
     */

    public void logOut(Long sessionTokenId){
        Optional<SessionToken> sessionToken = sessionTokenRepository.findById(sessionTokenId);
        sessionToken.ifPresent(token -> {
            token.getUser().setCounter(token.getUser().getCounter()+1);
            sessionTokenRepository.deleteById(sessionTokenId);
            userRepository.save(token.getUser());
        }); // to invalidate the JWT token and remove longlived session
    }

    /**
     * Method to change the stored password for the supplied user
     * @param user The user object for whom to change the password
     * @param password The new password to set for the supplied user
     * @return true if password successfully changed
     */

    public boolean setPassword(User user, String password) {
        user.getPasswordAuth().setPassword(passwordEncoder.encode(password));
        userRepository.save(user);
        Optional<VerificationToken> oToken = verificationTokenRepository.findByUser(user);
        oToken.ifPresent(token -> verificationTokenRepository.delete(token));
        return true;
    }

    /**
     * Method called when a user wants to reset its password
     * @param user The User object for whom to reset the password
     * @return a String holding the token that verifies the user
     */

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

    /**
     * get an Optional holding the User object corresponding to the userId
     * @param userid a Long identifying the user
     * @return an Optional holding the User
     */

    public Optional<User> getUserById(Long userid){
        return userRepository.findById(userid);
    }

    /**
     * Returns a User object corresponding to the supplied e´´ail
     * @param email a String holding an email to search for
     * @return a User object if it exist
     */

    public User getByEmail(String email){
        return passwordAuthRepository.getByEmail(email)
                .map(PasswordAuth::getUser).get();
    }

    /**
     * Finds a SessionToken object for the corresponding User
     * @param user the User for whom to find a SessionToken
     * @return A SessionToken object that corresponds to the User
     */
    public SessionToken getSessionTokenByUser(User user) {
        return sessionTokenRepository.findByUser(user).get();
    }

    /**
     * Saves a passed in SessionToken into the dataabse
     * @param token the token to store in the database
     * @return the SessionToken stored
     */

    public SessionToken saveSessionToken(SessionToken token){
        return sessionTokenRepository.save(token);
    }

    /**
     * Retreives a SessionToken object from the database that corresponds to the Striong
     * @param token the String to search for
     * @return the SessionToken object
     */

    public Optional<SessionToken> getSessionTokenByToken(String token){
        return sessionTokenRepository.findBySessionToken(token);
    }

    public Optional<User> getBySessionToken(SessionToken token){
        return userRepository.findById(token.getUser().getId());
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<SessionToken> findById(Long id){
        return sessionTokenRepository.findById(id);
    }

}
