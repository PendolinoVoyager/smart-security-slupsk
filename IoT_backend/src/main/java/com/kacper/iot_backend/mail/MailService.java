package com.kacper.iot_backend.mail;

import com.kacper.iot_backend.user.User;
import jakarta.mail.MessagingException;
import org.springframework.stereotype.Service;

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

    public void sendResetPasswordMail(User user, String resetPasswordToken) throws MessagingException {
        MailMessage mailMessage = createMailMessage(user, resetPasswordToken);
        mailProducer.sendResetPasswordMailMessage(mailMessage);

    }

    private MailMessage createMailMessage(User user, String activationToken) {
        return MailMessage.builder()
                .email(user.getEmail())
                .name(user.getName())
                .token(activationToken)
                .build();
    }

}
