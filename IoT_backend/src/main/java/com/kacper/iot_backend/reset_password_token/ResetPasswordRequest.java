package com.kacper.iot_backend.reset_password_token;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record ResetPasswordRequest(
        @Email(message = "Email is invalid")
        @NotBlank(message = "Email is required")
        String mail
) {
}
