package com.example.demo.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class MessageProducer {

    private static final Logger log = LoggerFactory.getLogger(MessageProducer.class);

    private static final String NOTIFICATION_QUEUE = "notification-queue";
    private static final String NOTIFICATION_PATTERN = "notification.send";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publica uma mensagem string na fila notification-queue
     *
     * @param message mensagem a ser publicada
     */
    public void sendMessage(String message) {
        try {
            log.info("[MessageProducer] Enviando mensagem: {}", message);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, message);
            log.info("[MessageProducer] Mensagem enviada com sucesso para a fila '{}'", NOTIFICATION_QUEUE);
        } catch (Exception e) {
            log.error("[MessageProducer] Erro ao enviar mensagem: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar mensagem para a fila: " + e.getMessage(), e);
        }
    }

    /**
     * Envia uma notificação de email no formato padrão
     *
     * @param to      destinatário do email
     * @param subject assunto do email
     * @param html    conteúdo HTML do email
     * @param text    conteúdo texto do email
     */
    public void sendEmailNotification(String to, String subject, String html, String text) {
        try {
            // Construir a estrutura da mensagem
            Map<String, Object> messageData = new HashMap<>();
            messageData.put("pattern", NOTIFICATION_PATTERN);

            Map<String, Object> data = new HashMap<>();
            data.put("to", to);
            data.put("subject", subject);
            data.put("html", html);
            data.put("text", text);

            messageData.put("data", data);

            // Serializar para JSON
            String jsonMessage = objectMapper.writeValueAsString(messageData);

            log.info("[MessageProducer] Enviando notificação de email para: {}", to);
            rabbitTemplate.convertAndSend(NOTIFICATION_QUEUE, jsonMessage);
            log.info("[MessageProducer] Notificação de email enviada com sucesso para a fila '{}'", NOTIFICATION_QUEUE);
        } catch (Exception e) {
            log.error("[MessageProducer] Erro ao enviar notificação de email: {}", e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar notificação de email para a fila: " + e.getMessage(), e);
        }
    }
}
