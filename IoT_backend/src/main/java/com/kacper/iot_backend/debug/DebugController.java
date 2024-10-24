package com.kacper.iot_backend.debug;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/debug")
public class DebugController
{
    @GetMapping("/user")
    public String userDebug() {
        return "User auth works";
    }

    @GetMapping("/admin")
    public String adminDebug() {
        return "Admin auth works";
    }
}
