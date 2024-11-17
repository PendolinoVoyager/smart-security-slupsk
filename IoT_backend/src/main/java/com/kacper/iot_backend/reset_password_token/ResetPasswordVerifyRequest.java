package com.kacper.iot_backend.reset_password_token;

public record ResetPasswordVerifyRequest(
        String email,
        String token,
        String newPassword
) {
}
