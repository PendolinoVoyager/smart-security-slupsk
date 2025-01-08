package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class ResetPasswordTokenRepositoryTest {

    @Autowired
    private ResetPasswordTokenRepository resetPasswordTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private ResetPasswordToken resetPasswordToken;

    @AfterEach
    public void cleanup() {
        if (resetPasswordToken != null) {
            resetPasswordTokenRepository.delete(resetPasswordToken);
        }
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void shouldSaveResetPasswordToken() {
        user = createTestUser();
        userRepository.save(user);

        resetPasswordToken = ResetPasswordToken.builder()
                .token("123456")
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiry
                .attempts(0)
                .user(user)
                .build();

        resetPasswordToken = resetPasswordTokenRepository.save(resetPasswordToken);

        assertThat(resetPasswordToken.getToken()).isEqualTo("123456");
    }

    @Test
    public void shouldFindResetPasswordTokenById() {
        user = createTestUser();
        userRepository.save(user);

        resetPasswordToken = ResetPasswordToken.builder()
                .token("123456")
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiry
                .attempts(0)
                .user(user)
                .build();

        resetPasswordToken = resetPasswordTokenRepository.save(resetPasswordToken);

        Optional<ResetPasswordToken> foundToken = resetPasswordTokenRepository.findById(resetPasswordToken.getId());

        assertThat(foundToken).isPresent();
        assertThat(foundToken.get().getToken()).isEqualTo(resetPasswordToken.getToken());
    }

    @Test
    public void shouldDeleteResetPasswordTokenByUserId() {
        user = createTestUser();
        userRepository.save(user);

        resetPasswordToken = ResetPasswordToken.builder()
                .token("123456")
                .createdAt(new Date())
                .expiredAt(new Date(System.currentTimeMillis() + 3600000)) // 1 hour expiry
                .attempts(0)
                .user(user)
                .build();

        resetPasswordToken = resetPasswordTokenRepository.save(resetPasswordToken);

        resetPasswordTokenRepository.deleteByUserId(user.getId());
        entityManager.flush();
        entityManager.clear();

        Optional<ResetPasswordToken> foundToken = resetPasswordTokenRepository.findById(resetPasswordToken.getId());
        assertThat(foundToken).isNotPresent();
    }

    private User createTestUser() {
        User user = new User();
        user.setName("Test");
        user.setLast_name("User");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);
        return user;
    }
}
