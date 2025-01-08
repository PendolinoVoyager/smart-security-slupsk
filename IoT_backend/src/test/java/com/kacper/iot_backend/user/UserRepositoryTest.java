package com.kacper.iot_backend.user;

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
public class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    private User user;

    @AfterEach
    public void cleanup() {
        if (user != null) {
            userRepository.delete(user);
        }
    }

    @Test
    public void shouldSaveUser() {
        user = new User();
        user.setName("John");
        user.setLast_name("Doe");
        user.setEmail("john.doe@example.com");
        user.setPassword("password123");
        user.setRole("USER");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        user = userRepository.save(user);

        assertThat(user.getId()).isNotNull();
        assertThat(user.getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    public void shouldFindUserByEmail() {
        user = new User();
        user.setName("Jane");
        user.setLast_name("Doe");
        user.setEmail("jane.doe@example.com");
        user.setPassword("password123");
        user.setRole("ADMIN");
        user.setCreated_at(new Date());
        user.setEnabled(true);

        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByEmail("jane.doe@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getEmail()).isEqualTo("jane.doe@example.com");
    }
}
