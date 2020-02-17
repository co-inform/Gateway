package eu.coinform.gateway.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class EmailService {

    private JavaMailSender emailSender;
    private SimpleMailMessage template;

    @Value("${spring.mail.username}")
    private String from;

    public EmailService(JavaMailSender emailSender, SimpleMailMessage template){
        this.emailSender = emailSender;
        this.template = template;
    }

    public void sendSimpleMessage(String to){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject(template.getSubject());
        message.setText(template.getText());
        emailSender.send(message);
    }

}
