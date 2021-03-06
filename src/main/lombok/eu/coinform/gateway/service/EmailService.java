package eu.coinform.gateway.service;

import eu.coinform.gateway.controller.forms.ExternalEvaluationForm;
import eu.coinform.gateway.module.iface.FeedbackRequester;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.util.Date;

@Service
@Slf4j
public class EmailService {

    private JavaMailSender emailSender;
    private TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String from;

    @Value("${email.verify}")
    private String verify;

    @Value("${email.reset}")
    private String reset;

    @Value("${email.successful}")
    private String success;

    EmailService(JavaMailSender emailSender, TemplateEngine templateEngine){
        this.emailSender = emailSender;
        this.templateEngine = templateEngine;
    }


    public void sendVerifyEmailMessage(String to, String link){
        SimpleMailMessage message = new SimpleMailMessage();
        log.debug("Send {} to {}", link, to);

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Coinform Verification email");
        message.setText(String.format(verify,to,link));
        log.debug("Message: {}", message);
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

    public void sendSuccessMessage(String to){
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Password successfully changed for the CoInform system");
        message.setText(String.format(success,to));
        emailSender.send(message);
    }

    public void sendFailedModuleRequestEmail(String to, String module, String info, Date date){
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(from);
        message.setTo(to);
        message.setSubject("Coinform Gateway failed a request to " + module);
        message.setText("The Coinform API GateWay failed a request to your module at "+ date +".\n\nPlease check the availability of your service. More info about the request below: \n\n"+ info);
        emailSender.send(message);
    }

    public void sendUserRequestedFactcheckFeedback(String to, FeedbackRequester feedbackRequester, ExternalEvaluationForm evaluation) {
        final MimeMessage mimeMessage = this.emailSender.createMimeMessage();
        try {
            final MimeMessageHelper message = new MimeMessageHelper(mimeMessage, true, "UTF-8");
            message.setFrom(from);
            message.setTo(to);
            message.setSubject("Requested factcheck completed");

            final Context ctx = new Context();
            ctx.setVariable("requester", feedbackRequester);
            ctx.setVariable("evaluation", evaluation);
            final String htmlContent = templateEngine.process("factcheck_feedback.html", ctx);

            message.setText(htmlContent, true);
            emailSender.send(mimeMessage);
        } catch (MessagingException ex) {
            log.error("sendUserRequestedFactcheckFeedback: {}\n requester: {}\n evaluation: {}\n stack trace: {}", ex.getMessage(), feedbackRequester, evaluation, ex.getStackTrace());
        }
    }
}
