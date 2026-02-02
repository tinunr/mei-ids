package com.example.demo.producer;

import com.example.demo.config.RabbitMqExampleConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleProducer {

    private static final Logger log = LoggerFactory.getLogger(ExampleProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    /**
     * Envia uma mensagem para a fila de exemplo
     */
    public void sendMessage(String message) {
        try {
            log.info("[ExampleProducer] Enviando mensagem: {}", message);
            rabbitTemplate.convertAndSend(
                    RabbitMqExampleConfig.EXCHANGE_NAME,
                    RabbitMqExampleConfig.ROUTING_KEY,
                    message);
            log.info("[ExampleProducer] Mensagem enviada com sucesso para o exchange '{}'",
                    RabbitMqExampleConfig.EXCHANGE_NAME);
        } catch (Exception e) {
            log.error("[ExampleProducer] Erro ao enviar mensagem: {}", e.getMessage(), e);
        }
    }
}
