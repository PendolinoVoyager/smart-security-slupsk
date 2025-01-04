package com.kacper.iot_backend.auth;

import jakarta.validation.constraints.*;

public record AuthRegistrationRequest(
        @NotBlank(message = "Name is required")
        @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters long")
        String name,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 50, message = "Last name must be between 2 and 50 characters long")
        String last_name,

        @Email(message = "Email is invalid")
        @NotBlank(message = "Email is required")
        String email,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        @Pattern(regexp = "^\\p{ASCII}*$", message = "Password cannot contain emojis")
        String password
) {
}
