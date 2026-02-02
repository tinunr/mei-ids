package com.example.demo.consumer;

import org.apache.camel.ProducerTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMqConsumer {

    private final ProducerTemplate producerTemplate;

    public RabbitMqConsumer(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.createCourse}")
    public void receive(String body) {
        // Forward raw JSON body to Camel route
        producerTemplate.sendBody("direct:createCourse", body);
    }
}
