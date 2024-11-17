package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.TokenExpiredException;
import com.kacper.iot_backend.exception.TooManyAttemptsException;
import com.kacper.iot_backend.exception.MailSendingException;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
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

    public DefaultResponse sendResetPasswordToken(ResetPasswordRequest resetPasswordRequest) {
        User user = userService.getUserOrThrow(resetPasswordRequest.mail());
        ResetPasswordToken resetPasswordToken = createAndSaveResetPasswordToken(user);
        sendResetPasswordEmail(user, resetPasswordToken);

        // TODO: jutor to zrobic bo juz mi sie nie chce dzisiaj
//        resetPasswordTokenRepository.delete(resetPasswordToken);

        return buildResponse("Reset password token has been sent");
    }

    public DefaultResponse resetPasswordToken(ResetPasswordVerifyRequest resetPasswordVerifyRequest) {
        User user = userService.getUserOrThrow(resetPasswordVerifyRequest.email());

        validateTokenOrThrow(user.getResetPasswordToken(), resetPasswordVerifyRequest.token());
        validateTokenExpiryOrThrow(user.getResetPasswordToken());
        validateAttemptsOrThrow(user.getResetPasswordToken());

        userService.setNewPassword(user, resetPasswordVerifyRequest.newPassword());

        return buildResponse("Password has been changed");
    }

    private ResetPasswordToken createAndSaveResetPasswordToken(User user) {
        ResetPasswordToken resetPasswordToken = buildResetPasswordToken(user);
        resetPasswordTokenRepository.save(resetPasswordToken);
        return resetPasswordToken;
    }

    private void sendResetPasswordEmail(User user, ResetPasswordToken resetPasswordToken) {
        try {
            mailService.sendResetPasswordMail(user, resetPasswordToken.getToken());
        } catch (Exception e) {
            throw new MailSendingException("Failed to send reset password email");
        }
    }

    private void validateTokenOrThrow(ResetPasswordToken resetPasswordToken, String providedToken) {
        if (resetPasswordToken == null) {
            throw new InvalidTokenException("Invalid or missing token");
        }

        if (!resetPasswordToken.getToken().equals(providedToken)) {
            incrementTokenAttempts(resetPasswordToken);
            throw new InvalidTokenException("Invalid or missing token");
        }
    }

    private void validateTokenExpiryOrThrow(ResetPasswordToken resetPasswordToken) {
        if (resetPasswordToken == null || resetPasswordToken.getExpiredAt().before(new Date())) {
            throw new TokenExpiredException("Token has expired");
        }
    }

    private void validateAttemptsOrThrow(ResetPasswordToken resetPasswordToken) {
        if (resetPasswordToken == null) {
            throw new InvalidTokenException("Invalid token");
        }

        if (resetPasswordToken.getAttempts() >= 3) {
            throw new TooManyAttemptsException("Too many attempts");
        }
    }

    private void incrementTokenAttempts(ResetPasswordToken resetPasswordToken) {
        int currentAttempts = resetPasswordToken.getAttempts() == null ? 0 : resetPasswordToken.getAttempts();
        resetPasswordToken.setAttempts(currentAttempts + 1);
        resetPasswordTokenRepository.save(resetPasswordToken);
    }

    private ResetPasswordToken buildResetPasswordToken(User user) {
        return ResetPasswordToken.builder()
                .token(generateResetPasswordToken())
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 600000))
                .user(user)
                .attempts(0)
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

    private DefaultResponse buildResponse(String message) {
        return DefaultResponse.builder()
                .message(message)
                .build();
    }
}
