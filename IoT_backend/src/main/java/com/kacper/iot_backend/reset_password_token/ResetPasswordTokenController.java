package com.kacper.iot_backend.reset_password_token;

import com.kacper.iot_backend.utils.DefaultResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.mail.MessagingException;
import org.springframework.http.ResponseEntity;
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

    @Operation(summary = "Send reset password token to user's email",
            description = "Generates and sends a reset password token to the provided email.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token sent successfully",
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = DefaultResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request",
                    content = @Content(mediaType = "application/json")),
            @ApiResponse(responseCode = "409", description = "Token already sent to this user",
                    content = @Content(mediaType = "application/json"))
    })
    @PostMapping("/send")
    public DefaultResponse sendResetPasswordToken(@RequestBody ResetPasswordRequest resetPasswordRequest) throws MessagingException {
        return resetPasswordTokenService.sendResetPasswordToken(resetPasswordRequest);
    }

    @PostMapping("/verify")
    public DefaultResponse verifyPasswordToken() {
        return null;
    }
}
