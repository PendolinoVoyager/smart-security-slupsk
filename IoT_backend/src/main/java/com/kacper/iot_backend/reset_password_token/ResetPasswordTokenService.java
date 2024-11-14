package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class ResetPasswordTokenService
{
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

    public DefaultResponse sendResetPasswordToken(ResetPasswordRequest resetPasswordRequest) {
        User user = userService.getUser(resetPasswordRequest.mail());
        ResetPasswordToken resetPasswordToken = buildResetPasswordToken(user);
        resetPasswordTokenRepository.save(resetPasswordToken);

        try {
            mailService.sendResetPasswordMail(user, resetPasswordToken.getToken());
            return DefaultResponse.builder()
                    .message("Reset password token has been sent to your email")
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("sraka");
        }

    }

    private ResetPasswordToken buildResetPasswordToken(User user) {
        return ResetPasswordToken.builder()
                .token(generateResetPasswordToken())
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 600000))
                .user(user)
                .build();
    }

    private String generateResetPasswordToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(6);

        for (int i = 0; i < 6; i++) {
            int index = random.nextInt(10);
            token.append(index);
        }

        return token.toString();
    }
}
