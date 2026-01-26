package com.kacper.iot_backend.mail;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailMessageTest {

    private MailMessage mailMessage;

    @BeforeEach
    void setUp() {
        mailMessage = MailMessage.builder()
                .email("test@example.com")
                .name("Test User")
                .token("test-token-123")
                .build();
    }

    // ===================== BUILDER TESTS =====================

    @Test
    void shouldCreateMailMessageUsingBuilder() {
        // Given & When
        MailMessage builtMessage = MailMessage.builder()
                .email("builder@test.com")
                .name("Builder User")
                .token("builder-token")
                .build();

        // Then
        assertNotNull(builtMessage);
        assertEquals("builder@test.com", builtMessage.getEmail());
        assertEquals("Builder User", builtMessage.getName());
        assertEquals("builder-token", builtMessage.getToken());
    }

    @Test
    void shouldCreateMailMessageWithNoArgsConstructor() {
        // Given & When
        MailMessage emptyMessage = new MailMessage();

        // Then
        assertNotNull(emptyMessage);
        assertNull(emptyMessage.getEmail());
        assertNull(emptyMessage.getName());
        assertNull(emptyMessage.getToken());
    }

    @Test
    void shouldCreateMailMessageWithAllArgsConstructor() {
        // When
        MailMessage fullMessage = new MailMessage(
                "full@example.com",
                "Full User",
                "full-token"
        );

        // Then
        assertNotNull(fullMessage);
        assertEquals("full@example.com", fullMessage.getEmail());
        assertEquals("Full User", fullMessage.getName());
        assertEquals("full-token", fullMessage.getToken());
    }

    // ===================== GETTER TESTS =====================

    @Test
    void shouldGetEmail() {
        assertEquals("test@example.com", mailMessage.getEmail());
    }

    @Test
    void shouldGetName() {
        assertEquals("Test User", mailMessage.getName());
    }

    @Test
    void shouldGetToken() {
        assertEquals("test-token-123", mailMessage.getToken());
    }

    // ===================== SETTER TESTS =====================

    @Test
    void shouldSetEmail() {
        // When
        mailMessage.setEmail("new@example.com");

        // Then
        assertEquals("new@example.com", mailMessage.getEmail());
    }

    @Test
    void shouldSetName() {
        // When
        mailMessage.setName("New Name");

        // Then
        assertEquals("New Name", mailMessage.getName());
    }

    @Test
    void shouldSetToken() {
        // When
        mailMessage.setToken("new-token-456");

        // Then
        assertEquals("new-token-456", mailMessage.getToken());
    }

    // ===================== EMAIL FORMAT TESTS =====================

    @Test
    void shouldHandleSimpleEmail() {
        // When
        mailMessage.setEmail("simple@example.com");

        // Then
        assertEquals("simple@example.com", mailMessage.getEmail());
    }

    @Test
    void shouldHandleEmailWithSubdomain() {
        // When
        mailMessage.setEmail("user@sub.domain.example.com");

        // Then
        assertEquals("user@sub.domain.example.com", mailMessage.getEmail());
    }

    @Test
    void shouldHandleEmailWithPlusSign() {
        // When
        mailMessage.setEmail("user+tag@example.com");

        // Then
        assertEquals("user+tag@example.com", mailMessage.getEmail());
    }

    @Test
    void shouldHandleEmailWithDots() {
        // When
        mailMessage.setEmail("first.last@example.com");

        // Then
        assertEquals("first.last@example.com", mailMessage.getEmail());
    }

    @Test
    void shouldHandleEmailWithNumbers() {
        // When
        mailMessage.setEmail("user123@example456.com");

        // Then
        assertEquals("user123@example456.com", mailMessage.getEmail());
    }

    // ===================== NAME TESTS =====================

    @Test
    void shouldHandleSimpleName() {
        // When
        mailMessage.setName("John");

        // Then
        assertEquals("John", mailMessage.getName());
    }

    @Test
    void shouldHandleNameWithSpace() {
        // When
        mailMessage.setName("John Doe");

        // Then
        assertEquals("John Doe", mailMessage.getName());
    }

    @Test
    void shouldHandleNameWithMultipleSpaces() {
        // When
        mailMessage.setName("John Michael Doe Jr.");

        // Then
        assertEquals("John Michael Doe Jr.", mailMessage.getName());
    }

    @Test
    void shouldHandleUnicodeName() {
        // When
        mailMessage.setName("Użytkownik Testowy");

        // Then
        assertEquals("Użytkownik Testowy", mailMessage.getName());
    }

    @Test
    void shouldHandleChineseName() {
        // When
        mailMessage.setName("测试用户");

        // Then
        assertEquals("测试用户", mailMessage.getName());
    }

    @Test
    void shouldHandleNameWithSpecialCharacters() {
        // When
        mailMessage.setName("O'Brien-Smith");

        // Then
        assertEquals("O'Brien-Smith", mailMessage.getName());
    }

    @Test
    void shouldHandleEmptyName() {
        // When
        mailMessage.setName("");

        // Then
        assertEquals("", mailMessage.getName());
    }

    // ===================== TOKEN TESTS =====================

    @Test
    void shouldHandleShortToken() {
        // When
        mailMessage.setToken("abc");

        // Then
        assertEquals("abc", mailMessage.getToken());
    }

    @Test
    void shouldHandleLongToken() {
        // Given
        String longToken = "a".repeat(500);

        // When
        mailMessage.setToken(longToken);

        // Then
        assertEquals(500, mailMessage.getToken().length());
    }

    @Test
    void shouldHandleUuidToken() {
        // When
        mailMessage.setToken("550e8400-e29b-41d4-a716-446655440000");

        // Then
        assertEquals("550e8400-e29b-41d4-a716-446655440000", mailMessage.getToken());
    }

    @Test
    void shouldHandleJwtLikeToken() {
        // Given
        String jwtToken = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NTY3ODkwIiwibmFtZSI6IkpvaG4gRG9lIiwiaWF0IjoxNTE2MjM5MDIyfQ.SflKxwRJSMeKKF2QT4fwpMeJf36POk6yJV_adQssw5c";

        // When
        mailMessage.setToken(jwtToken);

        // Then
        assertEquals(jwtToken, mailMessage.getToken());
    }

    @Test
    void shouldHandleTokenWithSpecialCharacters() {
        // When
        mailMessage.setToken("token+special/chars=");

        // Then
        assertEquals("token+special/chars=", mailMessage.getToken());
    }

    // ===================== NULL HANDLING TESTS =====================

    @Test
    void shouldHandleNullEmail() {
        // When
        mailMessage.setEmail(null);

        // Then
        assertNull(mailMessage.getEmail());
    }

    @Test
    void shouldHandleNullName() {
        // When
        mailMessage.setName(null);

        // Then
        assertNull(mailMessage.getName());
    }

    @Test
    void shouldHandleNullToken() {
        // When
        mailMessage.setToken(null);

        // Then
        assertNull(mailMessage.getToken());
    }

    // ===================== EQUALITY TESTS (via @Data) =====================

    @Test
    void shouldBeEqualWithSameValues() {
        // Given
        MailMessage message1 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token")
                .build();

        MailMessage message2 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token")
                .build();

        // Then
        assertEquals(message1, message2);
    }

    @Test
    void shouldNotBeEqualWithDifferentEmail() {
        // Given
        MailMessage message1 = MailMessage.builder()
                .email("test1@example.com")
                .name("Test")
                .token("token")
                .build();

        MailMessage message2 = MailMessage.builder()
                .email("test2@example.com")
                .name("Test")
                .token("token")
                .build();

        // Then
        assertNotEquals(message1, message2);
    }

    @Test
    void shouldNotBeEqualWithDifferentName() {
        // Given
        MailMessage message1 = MailMessage.builder()
                .email("test@example.com")
                .name("Test1")
                .token("token")
                .build();

        MailMessage message2 = MailMessage.builder()
                .email("test@example.com")
                .name("Test2")
                .token("token")
                .build();

        // Then
        assertNotEquals(message1, message2);
    }

    @Test
    void shouldNotBeEqualWithDifferentToken() {
        // Given
        MailMessage message1 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token1")
                .build();

        MailMessage message2 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token2")
                .build();

        // Then
        assertNotEquals(message1, message2);
    }

    // ===================== HASH CODE TESTS =====================

    @Test
    void shouldHaveSameHashCodeForEqualObjects() {
        // Given
        MailMessage message1 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token")
                .build();

        MailMessage message2 = MailMessage.builder()
                .email("test@example.com")
                .name("Test")
                .token("token")
                .build();

        // Then
        assertEquals(message1.hashCode(), message2.hashCode());
    }

    // ===================== TO STRING TESTS =====================

    @Test
    void shouldGenerateToString() {
        // When
        String toString = mailMessage.toString();

        // Then
        assertNotNull(toString);
        assertTrue(toString.contains("test@example.com"));
        assertTrue(toString.contains("Test User"));
        assertTrue(toString.contains("test-token-123"));
    }

    // ===================== BUILDER PARTIAL TESTS =====================

    @Test
    void shouldBuildWithOnlyEmail() {
        // When
        MailMessage partialMessage = MailMessage.builder()
                .email("only@email.com")
                .build();

        // Then
        assertNotNull(partialMessage);
        assertEquals("only@email.com", partialMessage.getEmail());
        assertNull(partialMessage.getName());
        assertNull(partialMessage.getToken());
    }

    @Test
    void shouldBuildWithOnlyName() {
        // When
        MailMessage partialMessage = MailMessage.builder()
                .name("Only Name")
                .build();

        // Then
        assertNotNull(partialMessage);
        assertNull(partialMessage.getEmail());
        assertEquals("Only Name", partialMessage.getName());
        assertNull(partialMessage.getToken());
    }

    @Test
    void shouldBuildWithOnlyToken() {
        // When
        MailMessage partialMessage = MailMessage.builder()
                .token("only-token")
                .build();

        // Then
        assertNotNull(partialMessage);
        assertNull(partialMessage.getEmail());
        assertNull(partialMessage.getName());
        assertEquals("only-token", partialMessage.getToken());
    }

    // ===================== EDGE CASE TESTS =====================

    @Test
    void shouldHandleWhitespaceOnlyEmail() {
        // When
        mailMessage.setEmail("   ");

        // Then
        assertEquals("   ", mailMessage.getEmail());
    }

    @Test
    void shouldHandleWhitespaceOnlyName() {
        // When
        mailMessage.setName("   ");

        // Then
        assertEquals("   ", mailMessage.getName());
    }

    @Test
    void shouldHandleWhitespaceOnlyToken() {
        // When
        mailMessage.setToken("   ");

        // Then
        assertEquals("   ", mailMessage.getToken());
    }

    @Test
    void shouldHandleNewlineInName() {
        // When
        mailMessage.setName("Test\nUser");

        // Then
        assertEquals("Test\nUser", mailMessage.getName());
    }

    @Test
    void shouldHandleTabInName() {
        // When
        mailMessage.setName("Test\tUser");

        // Then
        assertEquals("Test\tUser", mailMessage.getName());
    }
}

