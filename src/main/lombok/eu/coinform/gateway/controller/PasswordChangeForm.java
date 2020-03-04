package eu.coinform.gateway.controller;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class PasswordChangeForm {

    @NotEmpty
    String oldPassword;

    @NotEmpty
    String newPassword;

}
