package eu.coinform.gateway.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

@Configuration
@Slf4j
public class EmailConfig {

    @Bean
    public SimpleMailMessage templateSimpleMessage() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setText("Please confirm your email %s by clicking the link %s");
        message.setSubject("Coinform Email verification");
        return message;
    }
}
