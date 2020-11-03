package eu.coinform.gateway.db;

import com.google.common.collect.Lists;
import eu.coinform.gateway.controller.forms.RegisterForm;
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
    private ModuleInfoRepository moduleInfoRepository;

    public UserDbManager(
            UserRepository userRepository,
            PasswordAuthRepository passwordAuthRepository,
            RoleRepository roleRepository,
            VerificationTokenRepository verificationTokenRepository,
            SessionTokenRepository sessionTokenRepository,
            ModuleInfoRepository moduleInfoRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordAuthRepository = passwordAuthRepository;
        this.roleRepository = roleRepository;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenRepository = sessionTokenRepository;
        this.moduleInfoRepository = moduleInfoRepository;
    }

    /**
     * Registers a new User with the CoInform system using the supplied email, password and list of roles
     *
     * @param form the RegisterFOrm containing the new user information
     * @param roleList a list of actual roles for the user
     * @return the created User
     * @throws UsernameAlreadyExistException
     */

    public User registerUser(RegisterForm form, List<RoleEnum> roleList) throws UsernameAlreadyExistException {
        if (passwordAuthRepository.existsByEmail(form.getEmail())) {
            throw new UsernameAlreadyExistException(form.getEmail());
        }

        User user = new User();
        user.setCreatedAt(new Date());
        User dbUser = userRepository.save(user);
        dbUser.setAcceptResearch(form.isResearch());
        dbUser.setAcceptCommunication(form.isCommunication());
        PasswordAuth passwordAuth = new PasswordAuth();
        passwordAuth.setEmail(form.getEmail().toLowerCase());
        passwordAuth.setPassword(passwordEncoder.encode(form.getPassword()));
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
     * Change password for the user belonging to the passed sessionTokenId.
     * @param sessionTokenId Long holding the sessionTokenId
     * @param newPassword the new password for the user
     * @param oldPassword the old password that is stored in the database
     * @return true if password successfully changed, otherwise false
     */

    public boolean passwordChange(Long sessionTokenId, String newPassword, String oldPassword){
        User user = sessionTokenRepository.findById(sessionTokenId).get().getUser();
        if(!passwordEncoder.matches(oldPassword, user.getPasswordAuth().getPassword())){
            return false;
        }
        user.getPasswordAuth().setPassword(passwordEncoder.encode(newPassword));
        List<SessionToken> list = user.getSessionTokenList();
        for(Iterator<SessionToken> iterator = list.iterator(); iterator.hasNext(); ) {
            SessionToken st = iterator.next();
            if(!st.getId().equals(sessionTokenId)) {
                iterator.remove();
                sessionTokenRepository.delete(st);
            } else {
                st.setCounter(st.getCounter()+1);
                sessionTokenRepository.save(st);
            }
        }
        user.setSessionTokenList(list);
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
     * @param sessionTokenId the sessionTokenId for the specific user whom to logout
     */

    public Optional<SessionToken> logOut(Long sessionTokenId) {
        Optional<SessionToken> sessionToken = sessionTokenRepository.findById(sessionTokenId);
        sessionTokenRepository.deleteById(sessionTokenId);
        return sessionToken;
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

        Date date = new Date();
        if(oToken.isEmpty()){
            //no token. create one, store it and send to the user
            VerificationToken myToken = new VerificationToken(user);
            verificationTokenRepository.save(myToken);
            token = myToken.getToken();
        } else if(oToken.get().checkExpiryDatePassed(date)) {
            //there is a token but it has expired. create a new one, store it and send it
            verificationTokenRepository.delete(oToken.get());
            VerificationToken myToken = new VerificationToken(user);
            verificationTokenRepository.save(myToken);
            token = myToken.getToken();
        } else if(!oToken.get().checkExpiryDatePassed(date)) {
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

    public Optional<User> getUserByUUID(String uuid) {
        return userRepository.findByUuid(uuid);
    }

    /**
     * Finds a SessionToken object for the corresponding User
     * @param user the User for whom to find a SessionToken
     * @return A SessionToken object that corresponds to the User
     */
    public List<SessionToken> getSessionTokenByUser(User user) {
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

    public Optional<User> getBySessionTokenId(Long sessionId){
        return sessionTokenRepository.findById(sessionId).map(SessionToken::getUser);
    }

    public Optional<SessionToken> getSessionToken(Long sessionId) {
        return sessionTokenRepository.findById(sessionId);
    }

    public User saveUser(User user) {
        return userRepository.save(user);
    }

    public Optional<SessionToken> findBySessionTokenId(Long id){
        return sessionTokenRepository.findById(id);
    }

    public Optional<ModuleInfo> findByModulename(String name) {
        return moduleInfoRepository.getByModulename(name);
    }

    public ModuleInfo saveModuleInfo(ModuleInfo module){
        return moduleInfoRepository.save(module);
    }

    public boolean existsByUuid(String uuid){
        return userRepository.existsByUuid(uuid);
    }


}
