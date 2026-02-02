package com.example.demo.controller;

import com.example.demo.producer.RabbitMqHealthCheckProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/rabbitmq")
public class RabbitMqTestController {

    private static final Logger log = LoggerFactory.getLogger(RabbitMqTestController.class);

    @Autowired
    private RabbitMqHealthCheckProducer healthCheckProducer;

    /**
     * Testa a conexão com RabbitMQ
     */
    @PostMapping("/health-check")
    public ResponseEntity<?> healthCheck() {
        try {
            log.info("[RabbitMqTestController] Health check endpoint called");
            healthCheckProducer.sendHealthCheck();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Health check message sent to RabbitMQ");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[RabbitMqTestController] Health check failed: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Testa enviando uma mensagem de sincronização de curso
     */
    @PostMapping("/test-sync-course")
    public ResponseEntity<?> testSyncCourse() {
        try {
            log.info("[RabbitMqTestController] Test sync course endpoint called");
            healthCheckProducer.sendTestSyncCourse();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Test sync course message sent to RabbitMQ");
            response.put("groupId", "TEST-001");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[RabbitMqTestController] Test sync course failed: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Testa enviando uma mensagem de criação de curso
     */
    @PostMapping("/test-create-course")
    public ResponseEntity<?> testCreateCourse() {
        try {
            log.info("[RabbitMqTestController] Test create course endpoint called");
            healthCheckProducer.sendTestCreateCourse();

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Test create course message sent to RabbitMQ");
            response.put("courseShortname", "TEST-CREATE");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[RabbitMqTestController] Test create course failed: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Testa a conexão básica
     */
    @GetMapping("/test")
    public ResponseEntity<?> testConnection() {
        try {
            log.info("[RabbitMqTestController] Test connection endpoint called");
            healthCheckProducer.sendTestMessage("moodle.sync.course.queue", "TEST_MESSAGE_BASIC");

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "RabbitMQ connection is working");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[RabbitMqTestController] Test connection failed: {}", e.getMessage(), e);
            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Retorna informações sobre os endpoints disponíveis
     */
    @GetMapping("/info")
    public ResponseEntity<?> info() {
        Map<String, Object> response = new HashMap<>();
        response.put("application", "MEI-IDS RabbitMQ Tester");
        response.put("version", "1.0.0");
        response.put("endpoints", new HashMap<String, String>() {
            {
                put("GET /api/rabbitmq/info", "Informações sobre os endpoints");
                put("GET /api/rabbitmq/test", "Teste básico de conexão com RabbitMQ");
                put("POST /api/rabbitmq/health-check", "Envia mensagem de health check");
                put("POST /api/rabbitmq/test-sync-course", "Teste de sincronização de curso");
                put("POST /api/rabbitmq/test-create-course", "Teste de criação de curso");
            }
        });
        response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

        return ResponseEntity.ok(response);
    }
}
