package com.kacper.iot_backend.reset_password_token;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ResetPasswordVerifyRequest(
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Token is required")
        String token,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        @Pattern(regexp = "^\\p{ASCII}*$", message = "Password cannot contain emojis")
        String newPassword
) {
}
