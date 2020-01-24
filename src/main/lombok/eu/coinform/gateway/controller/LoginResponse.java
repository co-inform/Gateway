package eu.coinform.gateway.controller;

import com.fasterxml.jackson.annotation.JsonView;
import eu.coinform.gateway.cache.Views;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class LoginResponse {
    @JsonView(Views.NoDebug.class)
    private String token;
}
