package eu.coinform.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private JavaMailSender emailSender;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${email.verify}")
    private String verify;

    @Value("${email.reset}")
    private String reset;

    EmailService(JavaMailSender emailSender){
        this.emailSender = emailSender;
    }

    public void sendVerifyEmailMessage(String to, String link){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Coinform Verification email");
        message.setText(String.format(verify,to,link));
        emailSender.send(message);
    }

    public void sendPasswordResetMessage(String to, String link){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Coinform Reset password email");
        message.setText(String.format(reset,link));
        emailSender.send(message);
    }

}
