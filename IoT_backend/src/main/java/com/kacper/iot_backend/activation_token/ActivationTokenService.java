package com.kacper.iot_backend.activation_token;

import com.kacper.iot_backend.user.User;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class ActivationTokenService
{
    public ActivationToken createActivationToken(User user) {
        return ActivationToken.builder()
                .token(generateActivationToken())
                .createdAt(new Date())
                .user(user)
                .build();
    }

    private String generateActivationToken() {
        SecureRandom random = new SecureRandom();
        StringBuilder token = new StringBuilder(4);

        for (int i = 0; i < 4; i++) {
            int index = random.nextInt(10);
            token.append(index);
        }

        return token.toString();
    }
}
