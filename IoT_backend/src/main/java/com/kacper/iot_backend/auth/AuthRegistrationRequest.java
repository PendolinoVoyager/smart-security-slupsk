package com.kacper.iot_backend.auth;

import jakarta.validation.constraints.*;

public record AuthRegistrationRequest(
        @NotBlank(message = "Name is required")
        @Min(value = 2, message = "Name must be at least 2 characters long")
        @Max(value = 50, message = "Name must be at most 50 characters long")
        String name,

        @NotBlank(message = "Last name is required")
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
