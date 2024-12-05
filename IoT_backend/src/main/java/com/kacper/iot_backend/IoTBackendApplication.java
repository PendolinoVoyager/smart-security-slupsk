package com.kacper.iot_backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
public class IoTBackendApplication
{

    public static void main(String[] args) {
        ApplicationContext context = SpringApplication.run(IoTBackendApplication.class, args);

        PasswordEncoder passwordEncoder = context.getBean(PasswordEncoder.class);
        System.out.println(passwordEncoder.encode("Password.123"));


    }

}
