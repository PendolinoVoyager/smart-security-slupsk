package com.kacper.iot_backend.mail;

import com.kacper.iot_backend.config.RabbitMQConfig;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailProducerTest {

    @Mock
    private RabbitTemplate rabbitTemplate;

    @InjectMocks
    private MailProducer mailProducer;

    private MailMessage mailMessage;

    @BeforeEach
    void setUp() {
        mailMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Test User")
                .token("activation-token-123")
                .build();
    }

    // ===================== SEND MAIL MESSAGE TESTS =====================

    @Test
    void shouldSendMailMessageToCorrectExchange() {
        // When
        mailProducer.sendMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.getMailExchange()),
                eq(RabbitMQConfig.getMailRoutingKey()),
                eq(mailMessage)
        );
    }

    @Test
    void shouldSendCorrectMailMessage() {
        // When
        mailProducer.sendMailMessage(mailMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        MailMessage sentMessage = captor.getValue();
        assertEquals("test@example.com", sentMessage.getEmail());
        assertEquals("Test User", sentMessage.getName());
        assertEquals("activation-token-123", sentMessage.getToken());
    }

    @Test
    void shouldUseCorrectRoutingKeyForVerificationMail() {
        // When
        mailProducer.sendMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate).convertAndSend(
                any(),
                eq(RabbitMQConfig.getMailRoutingKey()),
                any(MailMessage.class)
        );
    }

    @Test
    void shouldCallRabbitTemplateOnceForSendMail() {
        // When
        mailProducer.sendMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(MailMessage.class));

    }

    // ===================== SEND RESET PASSWORD MESSAGE TESTS =====================

    @Test
    void shouldSendResetPasswordMessageToCorrectExchange() {
        // When
        mailProducer.sendResetPasswordMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.getMailExchange()),
                eq(RabbitMQConfig.getResetPasswordRoutingKey()),
                eq(mailMessage)
        );
    }

    @Test
    void shouldSendCorrectResetPasswordMessage() {
        // Given
        MailMessage resetMessage = MailMessage.builder()
                .email("reset@example.com")
                .name("Reset User")
                .token("reset-token-456")
                .build();

        // When
        mailProducer.sendResetPasswordMailMessage(resetMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());

        MailMessage sentMessage = captor.getValue();
        assertEquals("reset@example.com", sentMessage.getEmail());
        assertEquals("Reset User", sentMessage.getName());
        assertEquals("reset-token-456", sentMessage.getToken());
    }

    @Test
    void shouldUseCorrectRoutingKeyForResetPasswordMail() {
        // When
        mailProducer.sendResetPasswordMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate).convertAndSend(
                any(),
                eq(RabbitMQConfig.getResetPasswordRoutingKey()),
                any(MailMessage.class)
        );
    }

    @Test
    void shouldCallRabbitTemplateOnceForResetPassword() {
        // When
        mailProducer.sendResetPasswordMailMessage(mailMessage);

        // Then
        verify(rabbitTemplate, times(1)).convertAndSend(any(), any(), any(MailMessage.class));

    }

    // ===================== DIFFERENT MESSAGE CONTENT TESTS =====================

    @Test
    void shouldSendMessageWithSpecialCharactersInEmail() {
        // Given
        MailMessage specialMessage = MailMessage.builder()
                .email("test+special@sub.example.com")
                .name("Test")
                .token("token")
                .build();

        // When
        mailProducer.sendMailMessage(specialMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());
        assertEquals("test+special@sub.example.com", captor.getValue().getEmail());
    }

    @Test
    void shouldSendMessageWithUnicodeInName() {
        // Given
        MailMessage unicodeMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Użytkownik Testowy 测试")
                .token("token")
                .build();

        // When
        mailProducer.sendMailMessage(unicodeMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());
        assertEquals("Użytkownik Testowy 测试", captor.getValue().getName());
    }

    @Test
    void shouldSendMessageWithLongToken() {
        // Given
        String longToken = "a".repeat(500);
        MailMessage longTokenMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token(longToken)
                .build();

        // When
        mailProducer.sendMailMessage(longTokenMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());
        assertEquals(500, captor.getValue().getToken().length());
    }

    @Test
    void shouldSendMessageWithEmptyName() {
        // Given
        MailMessage emptyNameMessage = MailMessage.builder()
                .email("test@example.com")
                .name("")
                .token("token")
                .build();

        // When
        mailProducer.sendMailMessage(emptyNameMessage);

        // Then
        ArgumentCaptor<MailMessage> captor = ArgumentCaptor.forClass(MailMessage.class);
        verify(rabbitTemplate).convertAndSend(any(), any(), captor.capture());
        assertEquals("", captor.getValue().getName());
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldNotInteractWithRabbitTemplateBeforeSend() {
        // Then
        verifyNoInteractions(rabbitTemplate);
    }

    @Test
    void shouldSendDifferentMessagesToCorrectQueues() {
        // Given
        MailMessage verificationMessage = MailMessage.builder()
                .email("verify@example.com")
                .name("Verify")
                .token("verify-token")
                .build();

        MailMessage resetMessage = MailMessage.builder()
                .email("reset@example.com")
                .name("Reset")
                .token("reset-token")
                .build();

        // When
        mailProducer.sendMailMessage(verificationMessage);
        mailProducer.sendResetPasswordMailMessage(resetMessage);

        // Then
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.getMailExchange()),
                eq(RabbitMQConfig.getMailRoutingKey()),
                eq(verificationMessage)
        );
        verify(rabbitTemplate).convertAndSend(
                eq(RabbitMQConfig.getMailExchange()),
                eq(RabbitMQConfig.getResetPasswordRoutingKey()),
                eq(resetMessage)
        );
    }
}

