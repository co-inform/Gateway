package eu.coinform.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/twitter/**")
                .allowedOrigins("https://twitter.com")
                .allowedOrigins("chrome://**")
                .allowedOrigins("chrome-extensions://**"); //should change to omairs actual endpoint but testing this first
        registry.addMapping("/response/**")
                .allowedOrigins("https://twitter.com");
        // added below as in Slack. dont know if correct though. Have added a second *
        // but not pushed to docker yet. Changed to above...
        /*registry.addMapping("chrome://**")
                .allowedOrigins("https://twitter,com");
        registry.addMapping("chrome-extension://**")
                .allowedOrigins("https://twitter.com");

         */
    }
}
