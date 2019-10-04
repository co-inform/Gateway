package eu.coinform.gateway.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class OpenAPIController {

    @GetMapping("/")
    public String openAPI() {
        return "openapi";
    }
}
