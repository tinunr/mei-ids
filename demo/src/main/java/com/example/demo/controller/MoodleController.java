package com.example.demo.controller;

import com.example.demo.bens.MoodleCourse;
import com.example.demo.bens.MoodleCourseRequest;
import com.example.demo.producer.MoodleCourseProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/moodle")
public class MoodleController {

    private static final Logger log = LoggerFactory.getLogger(MoodleController.class);

    @Autowired
    private MoodleCourseProducer moodleCourseProducer;

    /**
     * Endpoint para criar um novo curso no Moodle
     * Recebe os dados do curso e envia para a fila RabbitMQ para processamento
     *
     * @param courseRequest objeto contendo a lista de cursos a criar
     * @return resposta com status da operação
     */
    @PostMapping("/create-course")
    public ResponseEntity<?> createCourse(@RequestBody MoodleCourseRequest courseRequest) {
        try {
            log.info("[MoodleController] Recebido request para criar curso no Moodle");

            // Validar se há cursos na solicitação
            if (courseRequest == null || courseRequest.getCourses() == null || courseRequest.getCourses().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("status", "ERROR");
                response.put("message", "Nenhum curso foi fornecido na solicitação");
                response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
                return ResponseEntity.badRequest().body(response);
            }

            log.debug("[MoodleController] Cursos recebidos: {}", courseRequest.getCourses().size());

            // Enviar a solicitação para o RabbitMQ
            moodleCourseProducer.sendCreateCourseRequest(courseRequest);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "SUCCESS");
            response.put("message", "Solicitação de criação de curso enviada com sucesso");
            response.put("coursesCount", courseRequest.getCourses().size());
            response.put("queue", "moodle-create-course-queues");
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("[MoodleController] Erro ao processar solicitação de criação de curso: {}",
                    e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }

    /**
     * Endpoint de teste para criar um curso de exemplo com dados aleatórios
     * Cada requisição cria um curso único com identificadores diferentes
     *
     * @return resposta com status do teste
     */
    @PostMapping("/test")
    public ResponseEntity<?> test() {
        try {
            log.info("[MoodleController] Testando endpoint de criação de curso com dados únicos");

            // Gerar identificadores únicos para cada requisição
            String uniqueId = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
            long timestamp = System.currentTimeMillis();

            MoodleCourse testCourse = new MoodleCourse();
            testCourse.setFullname("Teste de Curso Novo - " + uniqueId);
            testCourse.setShortname("curso-" + timestamp);
            testCourse.setCategoryid(1);
            testCourse.setIdnumber("TST-" + uniqueId);
            testCourse.setSummary(
                    "Curso de teste criado em " + LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));
            testCourse.setSummaryformat(1);
            testCourse.setFormat("topics");
            testCourse.setVisible(1);

            log.debug("[MoodleController] Testando com dados únicos - ID: {}, Timestamp: {}", uniqueId, timestamp);

            MoodleCourseRequest courseRequest = new MoodleCourseRequest();
            ArrayList<MoodleCourse> courses = new ArrayList<>();
            courses.add(testCourse);
            courseRequest.setCourses(courses);

            return createCourse(courseRequest);
        } catch (Exception e) {
            log.error("[MoodleController] Erro ao testar endpoint: {}", e.getMessage(), e);

            Map<String, Object> response = new HashMap<>();
            response.put("status", "ERROR");
            response.put("message", e.getMessage());
            response.put("timestamp", LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME));

            return ResponseEntity.status(500).body(response);
        }
    }
}
