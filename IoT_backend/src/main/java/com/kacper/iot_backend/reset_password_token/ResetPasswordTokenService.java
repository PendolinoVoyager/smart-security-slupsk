package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.exception.ResourceAlreadyExistException;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class ResetPasswordTokenService {
    private final UserService userService;
    private final MailService mailService;
    private final ResetPasswordTokenRepository resetPasswordTokenRepository;

    public ResetPasswordTokenService(
            UserService userService,
            MailService mailService,
            ResetPasswordTokenRepository resetPasswordTokenRepository
    ) {
        this.userService = userService;
        this.mailService = mailService;
        this.resetPasswordTokenRepository = resetPasswordTokenRepository;
    }

    public DefaultResponse sendResetPasswordToken(ResetPasswordRequest resetPasswordRequest) throws MessagingException {
        User user = userService.getUser(resetPasswordRequest.mail());
        ensureNoExistingToken(user);

        ResetPasswordToken resetPasswordToken = createAndSaveToken(user);
        sendTokenEmail(user, resetPasswordToken.getToken());

        return DefaultResponse.builder()
                .message("Reset password token has been sent to your email")
                .build();
    }


    private void ensureNoExistingToken(User user) {
        if (resetPasswordTokenRepository.existsByUser(user)) {
            throw new ResourceAlreadyExistException("Reset password token already sent to this user.");
        }
    }

    private ResetPasswordToken createAndSaveToken(User user) {
        ResetPasswordToken resetPasswordToken = buildResetPasswordToken(user);
        return resetPasswordTokenRepository.save(resetPasswordToken);
    }

    private ResetPasswordToken buildResetPasswordToken(User user) {
        return ResetPasswordToken.builder()
                .token(generateResetPasswordToken())
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 600000))
                .attempts(0)
                .user(user)
                .build();
    }

    private void sendTokenEmail(User user, String token) throws MessagingException {
        try {
            mailService.sendResetPasswordMail(user, token);
        }  catch (MessagingException e) {
            throw new MessagingException("Error during sending verification mail");
        }
    }

    private String generateResetPasswordToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            token.append(random.nextInt(10));
        }

        return token.toString();
    }
}
