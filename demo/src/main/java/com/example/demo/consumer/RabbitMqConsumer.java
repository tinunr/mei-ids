package com.example.demo.consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.ProducerTemplate;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.rabbitmq", name = "enabled", havingValue = "true", matchIfMissing = false)
public class RabbitMqConsumer {

    private final ProducerTemplate producerTemplate;
    private static final Logger log = LoggerFactory.getLogger(RabbitMqConsumer.class);

    public RabbitMqConsumer(ProducerTemplate producerTemplate) {
        this.producerTemplate = producerTemplate;
    }

    @RabbitListener(queues = "${app.rabbitmq.queue.createCourse}")
    public void receive(String body) {
        // Forward raw JSON body to Camel route
        producerTemplate.sendBody("direct:createCourse", body);
    }

    /**
     * Escuta mensagens da fila de sincronização de cursos
     */
    @RabbitListener(queues = "${app.rabbitmq.queue.syncCourse}")
    public void receiveSyncCourse(String message) {
        log.info("[RabbitMqConsumer] Received sync course message from RabbitMQ");
        log.info("[RabbitMqConsumer] Message: {}", message);

        try {
            // Encaminha a mensagem JSON para a rota Camel
            producerTemplate.sendBody("direct:syncCourse", message);
            log.info("[RabbitMqConsumer] Message sent to direct:syncCourse route");
        } catch (Exception e) {
            log.error("[RabbitMqConsumer] Error processing sync course message: {}", e.getMessage(), e);
        }
    }
}
