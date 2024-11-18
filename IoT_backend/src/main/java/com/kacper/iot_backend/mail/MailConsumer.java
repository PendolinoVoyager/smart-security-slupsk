package com.kacper.iot_backend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailConsumer {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    @Value("${spring.mail.username}")
    private String emailAddress;

    public MailConsumer(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @RabbitListener(queues = "${rabbitmq.mail.queue}")
    public void receiveMailMessage(MailMessage mailMessage) throws MessagingException {
        sendMail(mailMessage, "Verification mail", "ActivationMail.html");
    }

    @RabbitListener(queues = "resetPasswordQueue")
    public void receiveResetPasswordMessage(MailMessage mailMessage) throws MessagingException {
        sendMail(mailMessage, "Reset Password Mail", "ResetPasswordMail.html");
    }

    private void sendMail(MailMessage mailMessage, String subject, String templateName) throws MessagingException {
        MimeMessage message = createMimeMessage(mailMessage, subject, templateName);
        javaMailSender.send(message);
    }

    private MimeMessage createMimeMessage(MailMessage mailMessage, String subject, String templateName) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom(emailAddress);
        helper.setTo(mailMessage.getEmail());
        helper.setSubject(subject);

        String htmlContent = generateHtmlContent(mailMessage, templateName);
        helper.setText(htmlContent, true);

        return message;
    }

    private String generateHtmlContent(MailMessage mailMessage, String templateName) {
        Context context = new Context();
        context.setVariable("name", mailMessage.getName());
        context.setVariable("activationToken", mailMessage.getToken());
        return templateEngine.process(templateName, context);
    }
}

