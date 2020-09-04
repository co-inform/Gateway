package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

@Data
public class RegisterForm {
    @Email
    String email;
    @NotEmpty
    String password;
    @NotNull
    boolean research;
    @NotNull
    boolean communication;
}
