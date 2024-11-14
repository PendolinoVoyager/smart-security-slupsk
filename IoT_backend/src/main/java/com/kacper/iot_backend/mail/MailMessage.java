package com.kacper.iot_backend.mail;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage
{
    private String email;
    private String name;
    private String token;
}
