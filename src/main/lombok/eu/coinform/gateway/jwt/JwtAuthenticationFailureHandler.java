package eu.coinform.gateway.jwt;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Slf4j
public class JwtAuthenticationFailureHandler implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, AuthenticationException e) throws IOException, ServletException {
        log.debug("auth failed: {}", e.getMessage());
        JwtErrorResponseContent errorResponseContent = new JwtErrorResponseContent();
        errorResponseContent.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        errorResponseContent.setError(e.getMessage());
        ObjectMapper mapper = new ObjectMapper();

        httpServletResponse.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        httpServletResponse.setContentType("application/json");
        mapper.writeValue(httpServletResponse.getWriter(), errorResponseContent);
    }
}
