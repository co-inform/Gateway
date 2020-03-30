package eu.coinform.gateway.db;

import com.fasterxml.jackson.databind.ObjectMapper;
import eu.coinform.gateway.controller.forms.LoginForm;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RegexRequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.io.IOException;


@Slf4j
public class UserDbAuthenticationFilter extends AbstractAuthenticationProcessingFilter {

    UserDbManager userDbManager;

    public UserDbAuthenticationFilter(AuthenticationManager authenticationManager, UserDbManager userDbManager) {
        super(new RegexRequestMatcher("/login.*", "POST", true));
        setAuthenticationManager(authenticationManager);
        this.userDbManager = userDbManager;
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {
        ObjectMapper objectMapper = new ObjectMapper();
        LoginForm loginForm;
        try {
            loginForm = objectMapper.readValue(request.getReader(), LoginForm.class);
        } catch (IOException ex) {
            //todo: should return a better error message
            throw new UserDbAuthenticationException(ex.getMessage());
        }
        return getAuthenticationManager().authenticate(new UsernamePasswordAuthenticationToken(loginForm.getEmail(), loginForm.getPassword()));
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContextHolder.getContext().setAuthentication(authResult);
        chain.doFilter(request, response);
    }
}
