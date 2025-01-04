package com.kacper.iot_backend.mail;

import com.kacper.iot_backend.mail.MailMessage;
import com.kacper.iot_backend.mail.MailProducer;
import com.kacper.iot_backend.mail.MailService;
import com.kacper.iot_backend.user.User;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class MailServiceTest {

    private MailProducer mailProducer;
    private MailService mailService;

    @BeforeEach
    void setUp() {
        mailProducer = mock(MailProducer.class);
        mailService = new MailService(mailProducer);
    }

    @Test
    void shouldSendVerificationMail() throws MessagingException {
        User user = createUser();
        String activationToken = "test-activation-token";

        mailService.sendVerificationMail(user, activationToken);

        ArgumentCaptor<MailMessage> mailMessageCaptor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailProducer, times(1)).sendMailMessage(mailMessageCaptor.capture());
        MailMessage capturedMessage = mailMessageCaptor.getValue();

        assertThat(capturedMessage.getEmail()).isEqualTo(user.getEmail());
        assertThat(capturedMessage.getName()).isEqualTo(user.getName());
        assertThat(capturedMessage.getToken()).isEqualTo(activationToken);
    }

    @Test
    void shouldSendResetPasswordMail() throws MessagingException {
        User user = createUser();
        String resetPasswordToken = "test-reset-token";

        mailService.sendResetPasswordMail(user, resetPasswordToken);

        ArgumentCaptor<MailMessage> mailMessageCaptor = ArgumentCaptor.forClass(MailMessage.class);
        verify(mailProducer, times(1)).sendResetPasswordMailMessage(mailMessageCaptor.capture());
        MailMessage capturedMessage = mailMessageCaptor.getValue();

        assertThat(capturedMessage.getEmail()).isEqualTo(user.getEmail());
        assertThat(capturedMessage.getName()).isEqualTo(user.getName());
        assertThat(capturedMessage.getToken()).isEqualTo(resetPasswordToken);
    }

    private User createUser() {
        User user = new User();
        user.setName("Test Name");
        user.setEmail("test@example.com");
        return user;
    }
}
