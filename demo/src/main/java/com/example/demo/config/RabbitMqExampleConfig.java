package com.example.demo.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqExampleConfig {

    // Exchange e Queue names
    public static final String EXCHANGE_NAME = "mei-ids-exchange-example";
    public static final String QUEUE_NAME = "mei-ids-queue-example";
    public static final String ROUTING_KEY = "mei-ids-routing-key";

    /**
     * Criar o Exchange Direct
     */
    @Bean
    public DirectExchange exampleExchange() {
        return new DirectExchange(EXCHANGE_NAME, true, false);
    }

    /**
     * Criar a Queue
     */
    @Bean
    public Queue exampleQueue() {
        return new Queue(QUEUE_NAME, true);
    }

    /**
     * Fazer o Binding entre Exchange e Queue
     */
    @Bean
    public Binding exampleBinding(Queue exampleQueue, DirectExchange exampleExchange) {
        return BindingBuilder.bind(exampleQueue)
                .to(exampleExchange)
                .with(ROUTING_KEY);
    }
}
