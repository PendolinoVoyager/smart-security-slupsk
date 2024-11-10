package com.kacper.iot_backend.mail;

import com.kacper.iot_backend.user.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
public class MailService
{
    private final MailProducer mailProducer;

    public MailService(
            MailProducer mailProducer
    ) {
        this.mailProducer = mailProducer;
    }

    public void sendVerificationMail(User user, String activationToken) throws MessagingException {
        MailMessage mailMessage = createMailMessage(user, activationToken);
        mailProducer.sendMailMessage(mailMessage);
    }

    private MailMessage createMailMessage(User user, String activationToken) {
        return MailMessage.builder()
                .email(user.getEmail())
                .name(user.getName())
                .activationToken(activationToken)
                .build();
    }

}
