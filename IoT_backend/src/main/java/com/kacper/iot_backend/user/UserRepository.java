package com.kacper.iot_backend.user;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

/**
 * @author Kacper Karabinowski
 */
public interface UserRepository extends JpaRepository<User, Integer> {
    Optional<User> findByEmail(String email);
}
