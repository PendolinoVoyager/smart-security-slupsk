package com.kacper.iot_backend.activation_token;

import com.kacper.iot_backend.exception.WrongLoginCredentialsException;
import com.kacper.iot_backend.user.User;
import com.kacper.iot_backend.user.UserService;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.util.Date;

@Service
public class ActivationTokenService
{
    private final UserService userService;
    private final ActivationTokenRepository activationTokenRepository;

    public ActivationTokenService(
            UserService userService,
            ActivationTokenRepository activationTokenRepository
    ) {
        this.userService = userService;
        this.activationTokenRepository = activationTokenRepository;
    }

    public ActivationToken createActivationToken(User user) {
        return ActivationToken.builder()
                .token(generateActivationToken())
                .createdAt(new Date())
                .user(user)
                .build();
    }

    // TODO: Add attempts limit + delete token after successful activation
    public void enableUser(ActivationTokenRequest activationTokenRequest) {
        if (verifyActivationToken(activationTokenRequest)) {
            User user = userService.getUser(activationTokenRequest.email());
            user.setEnabled(true);
            userService.saveUser(user);
        }
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

    private boolean verifyActivationToken(ActivationTokenRequest activationTokenRequest) {
        User user = userService.getUser(activationTokenRequest.email());
        ActivationToken activationToken = user.getActivationToken();

        if (!activationToken.getToken().equals(activationTokenRequest.activationToken())) {
            throw new WrongLoginCredentialsException("Invalid activation token");
        }

        return true;
    }
}
