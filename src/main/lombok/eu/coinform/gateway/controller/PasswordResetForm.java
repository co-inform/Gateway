package eu.coinform.gateway.controller;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class PasswordResetForm {

    @Email
    @NotEmpty
    String email;
}
