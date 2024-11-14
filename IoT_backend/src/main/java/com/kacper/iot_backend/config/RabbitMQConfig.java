package com.kacper.iot_backend.config;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.support.converter.Jackson2JsonMessageConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.logging.Logger;

@Configuration
public class RabbitMQConfig
{
    private static String MAIL_QUEUE;
    private static String MAIL_EXCHANGE;
    private static String MAIL_ROUTING_KEY;

    private static String RESET_PASSWORD_QUEUE;
    private static String RESET_PASSWORD_ROUTING_KEY;

    public Logger logger = Logger.getLogger(RabbitMQConfig.class.getName());

    public RabbitMQConfig(
            @Value("${rabbitmq.mail.queue}") String mailQueue,
            @Value("${rabbitmq.mail.exchange}") String mailExchange,
            @Value("${rabbitmq.mail.routing-key}") String mailRoutingKey,
            @Value("${rabbitmq.reset-password.queue}") String resetPasswordQueue,
            @Value("${rabbitmq.reset-password.routing-key}") String resetPasswordRoutingKey
    ) {
        MAIL_QUEUE = mailQueue;
        MAIL_EXCHANGE = mailExchange;
        MAIL_ROUTING_KEY = mailRoutingKey;

        RESET_PASSWORD_QUEUE = resetPasswordQueue;
        RESET_PASSWORD_ROUTING_KEY = resetPasswordRoutingKey;
    }

    public static String getMailQueue() {
        return MAIL_QUEUE;
    }

    public static String getMailExchange() {
        return MAIL_EXCHANGE;
    }

    public static String getMailRoutingKey() {
        return MAIL_ROUTING_KEY;
    }

    public static String getResetPasswordQueue() {
        return RESET_PASSWORD_QUEUE;
    }

    public static String getResetPasswordRoutingKey() {
        return RESET_PASSWORD_ROUTING_KEY;
    }

    // Mail Queue and Exchange
    @Bean
    public Queue mailQueue() {
        logger.info("Creating mail queue " + MAIL_QUEUE);
        return new Queue(MAIL_QUEUE, true);
    }

    @Bean
    public TopicExchange mailExchange() {
        logger.info("Creating mail exchange " + MAIL_EXCHANGE);
        return new TopicExchange(MAIL_EXCHANGE);
    }

    @Bean
    public Binding mailBinding(Queue mailQueue, TopicExchange mailExchange) {
        logger.info("Binding mail queue to mail exchange " + MAIL_EXCHANGE + " with routing key " + MAIL_ROUTING_KEY);
        return BindingBuilder.bind(mailQueue).to(mailExchange).with(MAIL_ROUTING_KEY);
    }

    @Bean
    public Queue resetPasswordQueue() {
        logger.info("Creating reset password queue " + RESET_PASSWORD_QUEUE);
        return new Queue(RESET_PASSWORD_QUEUE, true);
    }

    @Bean
    public Binding resetPasswordBinding(Queue resetPasswordQueue, TopicExchange mailExchange) {
        logger.info("Binding reset password queue to mail exchange " + MAIL_EXCHANGE + " with routing key " + RESET_PASSWORD_ROUTING_KEY);
        return BindingBuilder.bind(resetPasswordQueue).to(mailExchange).with(RESET_PASSWORD_ROUTING_KEY);
    }

    @Bean
    public Jackson2JsonMessageConverter jsonMessageConverter() {
        return new Jackson2JsonMessageConverter();
    }

    @Bean
    public ConnectionFactory connectionFactory() {
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("localhost");
        connectionFactory.setUsername("guest");
        connectionFactory.setPassword("guest");
        connectionFactory.setVirtualHost("/");
        return connectionFactory;
    }

    @Bean
    public RabbitTemplate rabbitTemplate(ConnectionFactory connectionFactory) {
        RabbitTemplate rabbitTemplate = new RabbitTemplate(connectionFactory);
        rabbitTemplate.setMessageConverter(jsonMessageConverter());
        return rabbitTemplate;
    }
}
