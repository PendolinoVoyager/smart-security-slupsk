package com.kacper.iot_backend.mail;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;
import com.kacper.iot_backend.config.RabbitMQConfig;

@Service
public class MailProducer {
    private final RabbitTemplate rabbitTemplate;

    public MailProducer(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void sendMailMessage(MailMessage mailMessage) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.getMailExchange(), RabbitMQConfig.getMailRoutingKey(), mailMessage);
    }

    public void sendResetPasswordMailMessage(MailMessage mailMessage) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.getMailExchange(), RabbitMQConfig.getResetPasswordRoutingKey(), mailMessage);
    }
}