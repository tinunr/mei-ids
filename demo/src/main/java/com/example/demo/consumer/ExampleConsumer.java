package com.example.demo.consumer;

import com.example.demo.config.RabbitMqExampleConfig;
import org.apache.camel.ProducerTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ExampleConsumer {

    private static final Logger log = LoggerFactory.getLogger(ExampleConsumer.class);

    @Autowired
    private ProducerTemplate producerTemplate;

    /**
     * Consumer que escuta a fila de exemplo e encaminha para a rota Camel
     */
    @RabbitListener(queues = RabbitMqExampleConfig.QUEUE_NAME)
    public void receiveMessage(String message) {
        log.info("[ExampleConsumer] Mensagem recebida da fila '{}': {}",
                RabbitMqExampleConfig.QUEUE_NAME, message);

        try {
            // Encaminha a mensagem para a rota Camel
            producerTemplate.sendBody("direct:exampleRoute", message);
            log.info("[ExampleConsumer] Mensagem encaminhada para a rota 'direct:exampleRoute'");
        } catch (Exception e) {
            log.error("[ExampleConsumer] Erro ao encaminhar mensagem: {}", e.getMessage(), e);
        }
    }
}
