package com.example.demo.controller;

import com.example.demo.producer.ExampleProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/example")
public class ExampleController {

    private static final Logger log = LoggerFactory.getLogger(ExampleController.class);

    @Autowired
    private ExampleProducer exampleProducer;

    /**
     * Endpoint para enviar mensagem para RabbitMQ
     */
    @PostMapping("/send-message")
    public ResponseEntity<?> sendMessage(
            @RequestParam(value = "message", defaultValue = "Ola estou aqui") String message) {

        try {
            log.info("[ExampleController] Recebido request para enviar mensagem: {}", message);

            // Enviar a mensagem para o RabbitMQ
            exampleProducer.sendMessage(message);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Mensagem enviada para RabbitMQ");
            response.put("payload", message);
            response.put("queue", "mei-ids-queue-example");
            response.put("exchange", "mei-ids-exchange-example");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[ExampleController] Erro ao enviar mensagem: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint para testar com mensagem padrão
     */
    @PostMapping("/test")
    public ResponseEntity<?> test() {
        return sendMessage("Ola estou aqui");
    }
}
