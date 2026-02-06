package com.example.demo.producer;

import com.example.demo.bens.MoodleCourseRequest;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoodleCourseProducer {

    private static final Logger log = LoggerFactory.getLogger(MoodleCourseProducer.class);

    private static final String MOODLE_CREATE_COURSE_QUEUE = "moodle-create-course-queues";

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Publica uma solicitação de criação de curso Moodle na fila
     *
     * @param courseRequest solicitação contendo os dados do curso
     */
    public void sendCreateCourseRequest(MoodleCourseRequest courseRequest) {
        try {
            log.info("[MoodleCourseProducer] Enviando solicitação de criação de curso para Moodle");

            String message = objectMapper.writeValueAsString(courseRequest);
            log.debug("[MoodleCourseProducer] Mensagem: {}", message);

            rabbitTemplate.convertAndSend(MOODLE_CREATE_COURSE_QUEUE, message);

            log.info("[MoodleCourseProducer] Solicitação enviada com sucesso para a fila '{}'",
                    MOODLE_CREATE_COURSE_QUEUE);
        } catch (Exception e) {
            log.error("[MoodleCourseProducer] Erro ao enviar solicitação de criação de curso: {}",
                    e.getMessage(), e);
            throw new RuntimeException("Erro ao enviar solicitação para a fila: " + e.getMessage(), e);
        }
    }
}
