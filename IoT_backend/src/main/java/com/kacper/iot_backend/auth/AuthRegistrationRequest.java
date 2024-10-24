package com.kacper.iot_backend.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record AuthRegistrationRequest(
        @NotBlank(message = "Name is required")
        String name,

        @NotBlank(message = "Last name is required")
        String last_name,

        @NotBlank(message = "Email is required")
        @Email(message = "Email is invalid")
        String email,

        @NotBlank(message = "Role is required")
        String role,

        @NotBlank(message = "Password cannot be empty")
        @Size(min = 8, message = "Password must be at least 8 characters long")
        @Pattern(regexp = ".*[!@#$%^&*(),.?\":{}|<>].*", message = "Password must contain at least one special character")
        @Pattern(regexp = "^\\p{ASCII}*$", message = "Password cannot contain emojis")
        String password
) {
}
