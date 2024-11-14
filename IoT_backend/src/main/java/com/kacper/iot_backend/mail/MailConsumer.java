package com.kacper.iot_backend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailConsumer {
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public MailConsumer(JavaMailSender javaMailSender, TemplateEngine templateEngine) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    @RabbitListener(queues = "${rabbitmq.mail.queue}")
    public void receiveMailMessage(MailMessage mailMessage) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("cinemasymfony@gmail.com");
        helper.setTo(mailMessage.getEmail());
        helper.setSubject("Verification mail");

        Context context = new Context();
        context.setVariable("name", mailMessage.getName());
        context.setVariable("activationToken", mailMessage.getToken());

        String htmlContent = templateEngine.process("ActivationMail.html", context);
        helper.setText(htmlContent, true);

        javaMailSender.send(message);
    }

    @RabbitListener(queues = "resetPasswordQueue")
    public void receiveResetPasswordMessage(MailMessage mailMessage) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

        helper.setFrom("cinemasymfony@gmail.com");
        helper.setTo(mailMessage.getEmail());
        helper.setSubject("Reset Password Mail");

        Context context = new Context();
        context.setVariable("name", mailMessage.getName());
        context.setVariable("resetPasswordToken", mailMessage.getToken());

        String htmlContent = templateEngine.process("ResetPasswordMail.html", context);
        helper.setText(htmlContent, true);

        javaMailSender.send(message);
    }
}
