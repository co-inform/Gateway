package eu.coinform.gateway.db;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.naming.AuthenticationException;
import java.io.IOException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UserNotVerifiedException extends AuthenticationException {

    public UserNotVerifiedException(String message) {
        super(message);
    }
}
