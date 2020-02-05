package eu.coinform.gateway.controller;

import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Data
public class LoginForm {
    @Email
    private String email;
    @NotEmpty
    private String password;
}