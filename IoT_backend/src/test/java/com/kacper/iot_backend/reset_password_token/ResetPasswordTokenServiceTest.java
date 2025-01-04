package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.exception.InvalidTokenException;
import com.kacper.iot_backend.exception.MailSendingException;
import com.kacper.iot_backend.exception.TokenExpiredException;
import com.kacper.iot_backend.exception.TooManyAttemptsException;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import com.kacper.iot_backend.utils.DefaultResponse;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

class ResetPasswordTokenServiceTest {

    private ResetPasswordTokenService resetPasswordTokenService;

    private UserService userService;
    private MailService mailService;
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        mailService = mock(MailService.class);
        resetPasswordTokenRepository = mock(ResetPasswordTokenRepository.class);

        resetPasswordTokenService = new ResetPasswordTokenService(
                userService,
                mailService,
                resetPasswordTokenRepository
        );
    }

    @Test
    void shouldSendResetPasswordTokenSuccessfully() throws MessagingException {
        User user = createUser();
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        when(userService.getUserOrThrow(request.mail())).thenReturn(user);

        DefaultResponse response = resetPasswordTokenService.sendResetPasswordToken(request);

        assertThat(response.message()).isEqualTo("Reset password token has been sent");
        verify(resetPasswordTokenRepository, times(1)).save(any(ResetPasswordToken.class));
        verify(mailService, times(1)).sendResetPasswordMail(eq(user), anyString());
    }

    @Test
    void shouldThrowExceptionWhenSendingEmailFails() throws MessagingException {
        User user = createUser();
        ResetPasswordRequest request = new ResetPasswordRequest("test@example.com");
        when(userService.getUserOrThrow(request.mail())).thenReturn(user);
        doThrow(new RuntimeException()).when(mailService).sendResetPasswordMail(any(User.class), anyString());

        assertThrows(MailSendingException.class, () -> resetPasswordTokenService.sendResetPasswordToken(request));
        verify(resetPasswordTokenRepository, times(1)).save(any(ResetPasswordToken.class));
        verify(mailService, times(1)).sendResetPasswordMail(eq(user), anyString());
    }

    @Test
    void shouldResetPasswordSuccessfully() {
        User user = createUser();
        ResetPasswordToken token = createResetPasswordToken(user);
        ResetPasswordVerifyRequest request = new ResetPasswordVerifyRequest(
                "test@example.com", "123456", "NewPassword1!"
        );
        user.setResetPasswordToken(token);
        when(userService.getUserOrThrow(request.email())).thenReturn(user);

        DefaultResponse response = resetPasswordTokenService.resetPasswordToken(request);

        assertThat(response.message()).isEqualTo("Password has been reset");
        verify(userService, times(1)).setNewPassword(eq(user), eq(request.newPassword()));
        verify(resetPasswordTokenRepository, times(1)).deleteByUserId(user.getId());
    }

    @Test
    void shouldThrowExceptionWhenTokenIsExpired() {
        User user = createUser();
        ResetPasswordToken token = createResetPasswordToken(user);
        token.setExpiredAt(new Date(System.currentTimeMillis() - 10000)); // Token już wygasł
        user.setResetPasswordToken(token);
        ResetPasswordVerifyRequest request = new ResetPasswordVerifyRequest(
                "test@example.com", "123456", "NewPassword1!"
        );
        when(userService.getUserOrThrow(request.email())).thenReturn(user);

        assertThrows(TokenExpiredException.class, () -> resetPasswordTokenService.resetPasswordToken(request));
        verify(resetPasswordTokenRepository, never()).deleteByUserId(anyInt());
    }

    @Test
    void shouldThrowExceptionWhenTooManyAttempts() {
        User user = createUser();
        ResetPasswordToken token = createResetPasswordToken(user);
        token.setAttempts(3);
        user.setResetPasswordToken(token);
        ResetPasswordVerifyRequest request = new ResetPasswordVerifyRequest(
                "test@example.com", "123456", "NewPassword1!"
        );
        when(userService.getUserOrThrow(request.email())).thenReturn(user);

        assertThrows(TooManyAttemptsException.class, () -> resetPasswordTokenService.resetPasswordToken(request));
        verify(resetPasswordTokenRepository, times(1)).deleteByUserId(user.getId());
    }

    private User createUser() {
        User user = new User();
        user.setId(1);
        user.setEmail("test@example.com");
        return user;
    }

    private ResetPasswordToken createResetPasswordToken(User user) {
        return ResetPasswordToken.builder()
                .token("123456")
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 600000))
                .attempts(0)
                .user(user)
                .build();
    }
}
