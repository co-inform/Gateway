package eu.coinform.gateway.controller.forms;

import lombok.Data;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class RegisterForm {
    @Email
    String email;
    @NotEmpty
    String password;
    boolean research;
    boolean communication;
}
