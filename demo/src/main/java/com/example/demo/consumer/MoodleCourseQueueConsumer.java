package com.example.demo.consumer;

import com.example.demo.bens.MoodleCourseRequest;
import com.example.demo.producer.MoodleCourseProducer;
import com.example.demo.services.MoodleService;
import com.example.demo.bens.CourseRequest;
import com.example.demo.bens.MoodleCourse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MoodleCourseQueueConsumer {

    private static final Logger log = LoggerFactory.getLogger(MoodleCourseQueueConsumer.class);

    @Autowired
    private MoodleService moodleService;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Listener para consumir mensagens da fila moodle-create-course-queues
     * Processa a criação de cursos no Moodle
     */
    @RabbitListener(queues = "moodle-create-course-queues")
    public void processCourseCreationRequest(String message) {
        try {
            log.info("[MoodleCourseQueueConsumer] Recebida mensagem da fila: {}", message);

            // Desserializar a mensagem
            MoodleCourseRequest courseRequest = objectMapper.readValue(message, MoodleCourseRequest.class);

            if (courseRequest == null || courseRequest.getCourses() == null || courseRequest.getCourses().isEmpty()) {
                log.warn("[MoodleCourseQueueConsumer] Nenhum curso fornecido na solicitação");
                return;
            }

            log.info("[MoodleCourseQueueConsumer] Processando {} cursos", courseRequest.getCourses().size());

            List<Map<String, Object>> results = new ArrayList<>();

            // Processar cada curso
            for (MoodleCourse course : courseRequest.getCourses()) {
                try {
                    // Validar campos obrigatórios
                    if (course.getFullname() == null || course.getFullname().isEmpty()) {
                        throw new IllegalArgumentException("Course fullname is required");
                    }
                    if (course.getShortname() == null || course.getShortname().isEmpty()) {
                        throw new IllegalArgumentException("Course shortname is required");
                    }
                    if (course.getCategoryid() == null) {
                        throw new IllegalArgumentException("Course categoryid is required");
                    }

                    log.info("[MoodleCourseQueueConsumer] Criando curso: {} ({})",
                            course.getFullname(), course.getShortname());

                    // Converter MoodleCourse para CourseRequest
                    CourseRequest request = convertMoodleCourseToCourseRequest(course);

                    // Criar o curso no Moodle
                    Map<String, Object> response = moodleService.createCourse(request);

                    Map<String, Object> courseResult = new HashMap<>();
                    courseResult.put("course", course.getFullname());
                    courseResult.put("status", "SUCCESS");
                    courseResult.put("response", response);
                    results.add(courseResult);

                    log.info("[MoodleCourseQueueConsumer] Curso criado com sucesso: {}", course.getFullname());

                } catch (Exception e) {
                    log.error("[MoodleCourseQueueConsumer] Erro ao criar curso {}: {}",
                            course.getFullname(), e.getMessage(), e);

                    Map<String, Object> courseResult = new HashMap<>();
                    courseResult.put("course", course.getFullname());
                    courseResult.put("status", "ERROR");
                    courseResult.put("error", e.getMessage());
                    results.add(courseResult);
                }
            }

            // Log do resultado final
            Map<String, Object> finalResult = new HashMap<>();
            finalResult.put("totalCourses", courseRequest.getCourses().size());
            finalResult.put("results", results);
            finalResult.put("timestamp", new Date());

            log.info("[MoodleCourseQueueConsumer] Processamento concluído: {}", finalResult);

        } catch (Exception e) {
            log.error("[MoodleCourseQueueConsumer] Erro ao processar mensagem: {}", e.getMessage(), e);
        }
    }

    /**
     * Converter MoodleCourse para CourseRequest para compatibilidade com
     * MoodleService
     */
    private CourseRequest convertMoodleCourseToCourseRequest(MoodleCourse moodleCourse) {
        CourseRequest courseRequest = new CourseRequest();
        courseRequest.setFullname(moodleCourse.getFullname());
        courseRequest.setShortname(moodleCourse.getShortname());
        courseRequest.setCategoryid(moodleCourse.getCategoryid());
        return courseRequest;
    }
}
