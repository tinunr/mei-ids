package com.example.demo.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitMqHealthCheckProducer {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqHealthCheckProducer.class);

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    @Value("${app.rabbitmq.exchange}")
    private String exchange;

    @Value("${app.rabbitmq.queue.syncCourse}")
    private String syncCourseQueue;

    @Value("${app.rabbitmq.queue.createCourse}")
    private String createCourseQueue;

    /**
     * Testa a conexão com RabbitMQ enviando uma mensagem de health check
     */
    public void sendHealthCheck() {
        try {
            Map<String, Object> healthCheckMessage = new HashMap<>();
            healthCheckMessage.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            healthCheckMessage.put("status", "HEALTHY");
            healthCheckMessage.put("message", "RabbitMQ Health Check");

            String messageJson = objectMapper.writeValueAsString(healthCheckMessage);

            rabbitTemplate.convertAndSend(exchange, syncCourseQueue, messageJson);

            log.info("[RabbitMqHealthCheckProducer] Health check message sent successfully");
            log.info("[RabbitMqHealthCheckProducer] Message: {}", messageJson);
        } catch (Exception e) {
            log.error("[RabbitMqHealthCheckProducer] Failed to send health check message: {}", e.getMessage(), e);
        }
    }

    /**
     * Testa enviando uma mensagem de teste simples
     */
    public void sendTestMessage(String queueName, String message) {
        try {
            rabbitTemplate.convertAndSend(exchange, queueName, message);
            log.info("[RabbitMqHealthCheckProducer] Test message sent to queue '{}': {}", queueName, message);
        } catch (Exception e) {
            log.error("[RabbitMqHealthCheckProducer] Failed to send test message: {}", e.getMessage(), e);
        }
    }

    /**
     * Simula uma mensagem de sincronização de curso
     */
    public void sendTestSyncCourse() {
        try {
            String testMessage = "{\n" +
                    "  \"groupId\": \"TEST-001\",\n" +
                    "  \"courseData\": {\n" +
                    "    \"fullname\": \"Teste Conexão RabbitMQ\",\n" +
                    "    \"shortname\": \"TEST-RABBITMQ\",\n" +
                    "    \"categoryid\": 1,\n" +
                    "    \"summary\": \"Mensagem de teste para verificar conexão\"\n" +
                    "  },\n" +
                    "  \"objectives\": \"Testar RabbitMQ\",\n" +
                    "  \"content\": \"Teste de conectividade\",\n" +
                    "  \"methodology\": \"Teste automático\",\n" +
                    "  \"evaluation\": \"N/A\",\n" +
                    "  \"bibliographyDescription\": \"N/A\",\n" +
                    "  \"students\": [],\n" +
                    "  \"teachers\": []\n" +
                    "}";

            rabbitTemplate.convertAndSend(exchange, syncCourseQueue, testMessage);
            log.info("[RabbitMqHealthCheckProducer] Test sync course message sent successfully");
        } catch (Exception e) {
            log.error("[RabbitMqHealthCheckProducer] Failed to send test sync course: {}", e.getMessage(), e);
        }
    }

    /**
     * Simula uma mensagem de criação de curso
     */
    public void sendTestCreateCourse() {
        try {
            String testMessage = "{\n" +
                    "  \"fullname\": \"Teste Criação de Curso\",\n" +
                    "  \"shortname\": \"TEST-CREATE\",\n" +
                    "  \"categoryid\": 1,\n" +
                    "  \"summary\": \"Mensagem de teste para criação de curso\"\n" +
                    "}";

            rabbitTemplate.convertAndSend(exchange, createCourseQueue, testMessage);
            log.info("[RabbitMqHealthCheckProducer] Test create course message sent successfully");
        } catch (Exception e) {
            log.error("[RabbitMqHealthCheckProducer] Failed to send test create course: {}", e.getMessage(), e);
        }
    }
}
