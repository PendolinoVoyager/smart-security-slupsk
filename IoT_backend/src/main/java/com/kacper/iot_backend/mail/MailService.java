package com.kacper.iot_backend.mail;

import com.kacper.iot_backend.activation_token.ActivationToken;
import com.kacper.iot_backend.user.User;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class MailService
{
    private final JavaMailSender javaMailSender;

    public MailService(
            JavaMailSender javaMailSender
    ) {
        this.javaMailSender = javaMailSender;
    }

    public void sendVerificationMail(User user, String activationToken) {
        SimpleMailMessage mailMessage = new SimpleMailMessage();

        mailMessage.setFrom("cinemasymfony@gmail.com");
        mailMessage.setTo(user.getEmail());
        mailMessage.setSubject("Verification mail");
        mailMessage.setText(
                "To verify your account use this code: " + activationToken
        );

        javaMailSender.send(mailMessage);

    }
}
