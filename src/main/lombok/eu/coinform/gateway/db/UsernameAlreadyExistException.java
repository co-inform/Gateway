package eu.coinform.gateway.db;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.io.IOException;

@ResponseStatus(code = HttpStatus.BAD_REQUEST)
public class UsernameAlreadyExistException extends IOException {

    UsernameAlreadyExistException(String username) {
        super("{\"status\": \"User already exists\"}");
    }

}
