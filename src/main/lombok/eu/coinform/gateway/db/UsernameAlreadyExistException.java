package eu.coinform.gateway.db;

import java.io.IOException;

public class UsernameAlreadyExistException extends IOException {

    UsernameAlreadyExistException(String username) {
        super(String.format("User %s already exist", username));
    }

}
