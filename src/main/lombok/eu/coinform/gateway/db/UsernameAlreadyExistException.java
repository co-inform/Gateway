package eu.coinform.gateway.db;

import java.io.IOException;

public class UsernameAlreadyExistException extends IOException {

    UsernameAlreadyExistException(String username) {
        super(String.format("user account for email '%s' already exists", username));
    }

}
