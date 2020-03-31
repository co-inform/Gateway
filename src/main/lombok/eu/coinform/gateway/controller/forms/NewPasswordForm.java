package eu.coinform.gateway.controller.forms;

import lombok.Data;

import javax.validation.constraints.NotEmpty;

@Data
public class NewPasswordForm {

    @NotEmpty
    String pw1;

    @NotEmpty
    String pw2;

    @NotEmpty
    String token;

}
