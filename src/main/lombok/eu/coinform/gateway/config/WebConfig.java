package eu.coinform.gateway.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@EnableWebMvc
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        /*
        registry.addMapping("/twitter/**")
                .allowedMethods("POST", "OPTIONS")
                .allowedOrigins("https://twitter.com, chrome://**, chrome-extension://**")
                .allowedHeaders("*");
        registry.addMapping("/response/**")
                .allowedMethods("GET", "OPTIONS")
                .allowedOrigins("https://twitter.com, chrome://**, chrome-extension://**")
                .allowedHeaders("*");
         */
        registry.addMapping("/**")
                .allowedMethods("GET", "POST", "OPTIONS")
                .allowedOrigins("*")
                .allowedHeaders("*");
    }
}
