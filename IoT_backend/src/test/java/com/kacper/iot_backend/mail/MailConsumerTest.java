package com.kacper.iot_backend.mail;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MailConsumerTest {

    @Mock
    private JavaMailSender javaMailSender;

    @Mock
    private TemplateEngine templateEngine;

    @Mock
    private MimeMessage mimeMessage;

    @InjectMocks
    private MailConsumer mailConsumer;

    private MailMessage mailMessage;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(mailConsumer, "emailAddress", "noreply@test.com");

        mailMessage = MailMessage.builder()
                .email("recipient@example.com")
                .name("Test User")
                .token("activation-token-123")
                .build();
    }

    // ===================== RECEIVE VERIFICATION MAIL TESTS =====================

    @Test
    void shouldProcessVerificationMail() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("ActivationMail.html"), any(Context.class)))
                .thenReturn("<html>Activation email content</html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(javaMailSender).send(mimeMessage);
        verify(templateEngine).process(eq("ActivationMail.html"), any(Context.class));
    }

    @Test
    void shouldUseCorrectTemplateForVerification() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(templateEngine).process(eq("ActivationMail.html"), any(Context.class));
    }

    @Test
    void shouldPassCorrectContextToVerificationTemplate() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertEquals("Test User", context.getVariable("name"));
        assertEquals("activation-token-123", context.getVariable("activationToken"));
    }

    // ===================== RECEIVE RESET PASSWORD MAIL TESTS =====================

    @Test
    void shouldProcessResetPasswordMail() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(eq("ResetPasswordMail.html"), any(Context.class)))
                .thenReturn("<html>Reset password content</html>");

        // When
        mailConsumer.receiveResetPasswordMessage(mailMessage);

        // Then
        verify(javaMailSender).send(mimeMessage);
        verify(templateEngine).process(eq("ResetPasswordMail.html"), any(Context.class));
    }

    @Test
    void shouldUseCorrectTemplateForResetPassword() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveResetPasswordMessage(mailMessage);

        // Then
        verify(templateEngine).process(eq("ResetPasswordMail.html"), any(Context.class));
    }

    @Test
    void shouldPassCorrectContextToResetPasswordTemplate() throws MessagingException {
        // Given
        MailMessage resetMessage = MailMessage.builder()
                .email("reset@example.com")
                .name("Reset User")
                .token("reset-token-456")
                .build();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveResetPasswordMessage(resetMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());

        Context context = contextCaptor.getValue();
        assertEquals("Reset User", context.getVariable("name"));
        assertEquals("reset-token-456", context.getVariable("activationToken"));
    }

    // ===================== MAIL SENDER TESTS =====================

    @Test
    void shouldCreateMimeMessage() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(javaMailSender).createMimeMessage();
    }

    @Test
    void shouldSendMimeMessage() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(javaMailSender).send(mimeMessage);
    }

    @Test
    void shouldCallSendExactlyOnce() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(javaMailSender, times(1)).send(any(MimeMessage.class));
    }

    // ===================== DIFFERENT MESSAGE CONTENT TESTS =====================

    @Test
    void shouldHandleSpecialCharactersInName() throws MessagingException {
        // Given
        MailMessage specialMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Test <User> & 'Friends'")
                .token("token")
                .build();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(specialMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertEquals("Test <User> & 'Friends'", contextCaptor.getValue().getVariable("name"));
    }

    @Test
    void shouldHandleUnicodeInName() throws MessagingException {
        // Given
        MailMessage unicodeMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Użytkownik Testowy 测试用户")
                .token("token")
                .build();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(unicodeMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertEquals("Użytkownik Testowy 测试用户", contextCaptor.getValue().getVariable("name"));
    }

    @Test
    void shouldHandleLongToken() throws MessagingException {
        // Given
        String longToken = "a".repeat(1000);
        MailMessage longTokenMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token(longToken)
                .build();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(longTokenMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertEquals(longToken, contextCaptor.getValue().getVariable("activationToken"));
    }

    @Test
    void shouldHandleEmptyName() throws MessagingException {
        // Given
        MailMessage emptyNameMessage = MailMessage.builder()
                .email("test@example.com")
                .name("")
                .token("token")
                .build();

        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        // When
        mailConsumer.receiveMailMessage(emptyNameMessage);

        // Then
        ArgumentCaptor<Context> contextCaptor = ArgumentCaptor.forClass(Context.class);
        verify(templateEngine).process(anyString(), contextCaptor.capture());
        assertEquals("", contextCaptor.getValue().getVariable("name"));
    }

    // ===================== TEMPLATE ENGINE TESTS =====================

    @Test
    void shouldProcessTemplateWithCorrectParameters() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class)))
                .thenReturn("<html><body>Hello Test User, your token is activation-token-123</body></html>");

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(templateEngine).process(eq("ActivationMail.html"), any(Context.class));
    }

    @Test
    void shouldUseHtmlContent() throws MessagingException {
        // Given
        String htmlContent = "<html><body><h1>Welcome!</h1></body></html>";
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn(htmlContent);

        // When
        mailConsumer.receiveMailMessage(mailMessage);

        // Then
        verify(templateEngine).process(anyString(), any(Context.class));
    }

    // ===================== VERIFICATION TESTS =====================

    @Test
    void shouldNotSendMailBeforeReceiving() {
        // Then
        verifyNoInteractions(javaMailSender);
    }

    @Test
    void shouldNotProcessTemplateBeforeReceiving() {
        // Then
        verifyNoInteractions(templateEngine);
    }

    @Test
    void shouldProcessBothVerificationAndResetMails() throws MessagingException {
        // Given
        when(javaMailSender.createMimeMessage()).thenReturn(mimeMessage);
        when(templateEngine.process(anyString(), any(Context.class))).thenReturn("<html></html>");

        MailMessage verifyMessage = MailMessage.builder()
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
        mailConsumer.receiveMailMessage(verifyMessage);
        mailConsumer.receiveResetPasswordMessage(resetMessage);

        // Then
        verify(templateEngine).process(eq("ActivationMail.html"), any(Context.class));
        verify(templateEngine).process(eq("ResetPasswordMail.html"), any(Context.class));
        verify(javaMailSender, times(2)).send(any(MimeMessage.class));
    }
}

