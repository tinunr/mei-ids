package com.example.demo.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MoodleRabbitMqConfig {

    // Queue name para criar cursos no Moodle
    public static final String MOODLE_CREATE_COURSE_QUEUE = "moodle-create-course-queues";

    /**
     * Criar a Queue para solicitações de criação de curso no Moodle
     */
    @Bean
    public Queue moodleCreateCourseQueue() {
        return new Queue(MOODLE_CREATE_COURSE_QUEUE, true);
    }
}
