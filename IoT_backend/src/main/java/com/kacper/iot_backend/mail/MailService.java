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
    private final JavaMailSender javaMailSender;
    private final TemplateEngine templateEngine;

    public MailService(
            JavaMailSender javaMailSender,
            TemplateEngine templateEngine
    ) {
        this.javaMailSender = javaMailSender;
        this.templateEngine = templateEngine;
    }

    public void sendVerificationMail(User user, String activationToken) throws MessagingException {
        MimeMessage mailMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(mailMessage, "utf-8");

        helper.setFrom("cinemasymfony@gmail.com");
        helper.setTo(user.getEmail());
        helper.setSubject("Verification mail");

        Context context = new Context();
        context.setVariable("name", user.getName());
        context.setVariable("activationToken", activationToken);

        String htmlContent = templateEngine.process("ActivationMail.html", context);
        helper.setText(htmlContent, true);

        javaMailSender.send(mailMessage);
    }

}
