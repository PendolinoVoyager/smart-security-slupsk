package com.kacper.iot_backend.activation_token;

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
import static org.junit.jupiter.api.Assertions.assertNull;

@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@DataJpaTest
public class ActivationTokenRepositoryTest {

    @Autowired
    private ActivationTokenRepository activationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager entityManager;

    private User user;
    private ActivationToken activationToken;

    @AfterEach
    public void cleanup() {
        if (activationToken != null) {
            activationTokenRepository.delete(activationToken);
        }
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void shouldSaveActivationToken() {
        user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(false);

        userRepository.save(user);

        activationToken = new ActivationToken();
        activationToken.setToken("test-token");
        activationToken.setCreatedAt(new Date());
        activationToken.setUser(user);

        activationToken = activationTokenRepository.save(activationToken);

        assertThat(activationToken.getToken()).isEqualTo("test-token");
    }

    @Test
    public void shouldDeleteActivationTokenByUserId() {
        User user = new User();
        user.setName("Test");
        user.setLast_name("Test");
        user.setEmail("test@example.com");
        user.setPassword("password");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(false);

        userRepository.save(user);

        ActivationToken activationToken = new ActivationToken();
        activationToken.setToken("test-token");
        activationToken.setCreatedAt(new Date());
        activationToken.setUser(user);

        activationTokenRepository.save(activationToken);

        activationTokenRepository.deleteByUserId(user.getId());

        entityManager.flush();
        entityManager.clear();

        boolean tokenExists = activationTokenRepository.findById(activationToken.getId()).isPresent();
        assertThat(tokenExists).isFalse();
    }


}
