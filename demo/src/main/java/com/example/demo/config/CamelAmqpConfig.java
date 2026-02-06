package com.example.demo.config;

import org.springframework.context.annotation.Configuration;

@Configuration
public class CamelAmqpConfig {
    // Camel AMQP será auto-configurado pelo Spring Boot
    // via spring.rabbitmq.* properties
}
