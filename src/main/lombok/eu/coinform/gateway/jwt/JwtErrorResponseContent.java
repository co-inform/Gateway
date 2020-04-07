package eu.coinform.gateway.jwt;

import lombok.Data;

@Data
public class JwtErrorResponseContent {
    private int status;
    private String error;
}
