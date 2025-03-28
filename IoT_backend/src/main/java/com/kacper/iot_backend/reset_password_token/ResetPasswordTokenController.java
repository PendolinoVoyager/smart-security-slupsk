package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.utils.DefaultResponse;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/reset-password-token")
public class ResetPasswordTokenController
{
    private final ResetPasswordTokenService resetPasswordTokenService;

    public ResetPasswordTokenController(ResetPasswordTokenService resetPasswordTokenService) {
        this.resetPasswordTokenService = resetPasswordTokenService;
    }

    @PostMapping("/send")
    public DefaultResponse sendResetPasswordToken(@Valid @RequestBody ResetPasswordRequest resetPasswordRequest) {
        return resetPasswordTokenService.sendResetPasswordToken(resetPasswordRequest);
    }

    @PostMapping("/reset")
    public DefaultResponse verifyResetPasswordToken(@Valid @RequestBody ResetPasswordVerifyRequest resetPasswordVerifyRequest) {
        return resetPasswordTokenService.resetPasswordToken(resetPasswordVerifyRequest);
    }
}
