package com.example.demo.services;

import com.example.demo.bens.CourseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.*;

@Service
public class MoodleService {

    private static final Logger log = LoggerFactory.getLogger(MoodleService.class);

    @Value("${app.moodle.url}")
    private String moodleUrl;

    @Value("${app.moodle.token}")
    private String moodleToken;

    @Value("${app.moodle.create-function:core_course_create_courses}")
    private String createFunction;

    @Value("${app.moodle.format:json}")
    private String format;

    private final RestTemplate restTemplate;

    public MoodleService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Create a course in Moodle using form-urlencoded data
     * 
     * @param request CourseRequest with course details
     * @return Response from Moodle API
     */
    public Map<String, Object> createCourse(CourseRequest request) {
        log.info("[MoodleService.createCourse] Iniciando criação de curso: {}", request.getFullname());
        try {
            // Build form data with proper Moodle parameter format
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();

            // Required parameters
            formData.add("wstoken", moodleToken);
            formData.add("wsfunction", createFunction);
            formData.add("moodlewsrestformat", format);

            // Course data in Moodle format: courses[0][fieldname]=value
            formData.add("courses[0][fullname]", request.getFullname());
            formData.add("courses[0][shortname]", request.getShortname());
            formData.add("courses[0][categoryid]", request.getCategoryid().toString());

            if (request.getSummary() != null && !request.getSummary().isEmpty()) {
                formData.add("courses[0][summary]", request.getSummary());
                formData.add("courses[0][summaryformat]", "1");
            }

            log.debug(
                    "[MoodleService.createCourse] Parâmetros da requisição: wstoken={}, wsfunction={}, fullname={}, shortname={}, categoryid={}",
                    "***", createFunction, request.getFullname(), request.getShortname(), request.getCategoryid());

            // Build URL
            String url = moodleUrl;
            log.info("[MoodleService.createCourse] URL do Moodle: {}", url);

            // Set headers for form-urlencoded
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

            // Create entity with form data
            HttpEntity<MultiValueMap<String, String>> entity = new HttpEntity<>(formData, headers);

            log.debug("[MoodleService.createCourse] Enviando POST request para: {}", url);

            // Make HTTP POST request
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("[MoodleService.createCourse] Resposta recebida - Status Code: {}", response.getStatusCode());
            log.debug("[MoodleService.createCourse] Corpo da resposta: {}", response.getBody());

            if (response.getStatusCode() == HttpStatus.OK) {
                Map<String, Object> body = response.getBody();

                // Check for Moodle error in response
                if (body != null && body.containsKey("exception")) {
                    String exception = (String) body.get("exception");
                    String message = (String) body.getOrDefault("message", "Erro desconhecido");
                    log.error("[MoodleService.createCourse] ERRO do Moodle: {} - {}", exception, message);
                    return createErrorResponse(String.format("Moodle Error: %s - %s", exception, message), 400);
                }

                log.info("[MoodleService.createCourse] Curso criado com sucesso! Resposta: {}", body);
                return body != null ? body : createSuccessResponse("Curso criado com sucesso");
            } else {
                log.error("[MoodleService.createCourse] Falha ao criar curso - Status: {} - Body: {}",
                        response.getStatusCode(), response.getBody());
                return createErrorResponse(
                        String.format("Failed to create course. HTTP Status: %d", response.getStatusCode().value()),
                        response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("[MoodleService.createCourse] EXCEÇÃO ao criar curso - Mensagem: {}", e.getMessage(), e);
            e.printStackTrace();
            return createErrorResponse(String.format("Erro ao criar curso: %s", e.getMessage()), 500);
        }
    }

    /**
     * Enroll a user in a Moodle course
     * 
     * @param userId   Moodle user ID
     * @param courseId Moodle course ID
     * @param roleId   Role ID (5 = student, 3 = teacher)
     * @return Response from Moodle API
     */
    public Map<String, Object> enrollUser(Integer userId, Integer courseId, Integer roleId) {
        log.info("[MoodleService.enrollUser] Enrolando usuário {} no curso {} com role {}", userId, courseId, roleId);

        try {
            Map<String, Object> enrollment = new HashMap<>();
            enrollment.put("userid", userId);
            enrollment.put("courseid", courseId);
            enrollment.put("roleid", roleId);

            Map<String, Object> payload = new HashMap<>();
            payload.put("enrolments", Collections.singletonList(enrollment));

            String url = UriComponentsBuilder.fromHttpUrl(moodleUrl)
                    .queryParam("wstoken", moodleToken)
                    .queryParam("wsfunction", "enrol_manual_enrol_users")
                    .queryParam("moodlewsrestformat", format)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            log.debug("[MoodleService.enrollUser] Enviando POST request para inscrição");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("[MoodleService.enrollUser] Resposta recebida - Status Code: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[MoodleService.enrollUser] Usuário inscrito com sucesso");
                return response.getBody();
            } else {
                log.error("[MoodleService.enrollUser] Falha ao inscrever usuário. Status: {}",
                        response.getStatusCode());
                return createErrorResponse("Failed to enroll user", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("[MoodleService.enrollUser] Erro ao inscrever usuário no Moodle: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage(), 500);
        }
    }

    /**
     * Create a user in Moodle
     * 
     * @param username  Username
     * @param firstName First name
     * @param lastName  Last name
     * @param email     Email address
     * @return Response from Moodle API
     */
    public Map<String, Object> createUser(String username, String firstName, String lastName, String email) {
        log.info("[MoodleService.createUser] Criando usuário no Moodle: {}", username);

        try {
            Map<String, Object> user = new HashMap<>();
            user.put("username", username);
            user.put("firstname", firstName);
            user.put("lastname", lastName);
            user.put("email", email != null && !email.isEmpty() ? email : "noreply@example.com");
            user.put("auth", "db");
            user.put("confirmed", 1);
            user.put("country", "CV");
            user.put("lang", "pt");
            user.put("timezone", "Atlantic/Cape_Verde");

            Map<String, Object> payload = new HashMap<>();
            payload.put("users", Collections.singletonList(user));

            String url = UriComponentsBuilder.fromHttpUrl(moodleUrl)
                    .queryParam("wstoken", moodleToken)
                    .queryParam("wsfunction", "core_user_create_users")
                    .queryParam("moodlewsrestformat", format)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            log.debug("[MoodleService.createUser] Enviando POST request para criação de usuário");
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            log.info("[MoodleService.createUser] Resposta recebida - Status Code: {}", response.getStatusCode());

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("[MoodleService.createUser] Usuário criado com sucesso: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("[MoodleService.createUser] Falha ao criar usuário. Status: {}", response.getStatusCode());
                return createErrorResponse("Failed to create user", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("[MoodleService.createUser] Erro ao criar usuário no Moodle: {}", e.getMessage(), e);
            return createErrorResponse(e.getMessage(), 500);
        }
    }

    /**
     * Get course by name from Moodle
     * 
     * @param courseName Course full name or short name
     * @return Course details or null if not found
     */
    public Map<String, Object> getCourseByName(String courseName) {
        log.info("Searching for course: {}", courseName);

        try {
            String url = UriComponentsBuilder.fromHttpUrl(moodleUrl)
                    .queryParam("wstoken", moodleToken)
                    .queryParam("wsfunction", "core_course_search_courses")
                    .queryParam("criterianame", "search")
                    .queryParam("criteriavalue", courseName)
                    .queryParam("moodlewsrestformat", format)
                    .toUriString();

            ResponseEntity<Map> response = restTemplate.getForEntity(url, Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                log.info("Course search completed: {}", response.getBody());
                return response.getBody();
            } else {
                return createErrorResponse("Course not found", 404);
            }

        } catch (Exception e) {
            log.error("Error searching for course in Moodle", e);
            return createErrorResponse(e.getMessage(), 500);
        }
    }

    /**
     * Update course sections with content
     * 
     * @param courseId  Moodle course ID
     * @param sectionId Section ID
     * @param summary   Section summary/content
     * @return Response from Moodle API
     */
    public Map<String, Object> updateCourseSection(Integer courseId, Integer sectionId, String summary) {
        log.info("Updating course section {} for course {}", sectionId, courseId);

        try {
            Map<String, Object> section = new HashMap<>();
            section.put("id", sectionId);
            section.put("summary", summary);

            Map<String, Object> payload = new HashMap<>();
            payload.put("sections", Collections.singletonList(section));

            String url = UriComponentsBuilder.fromHttpUrl(moodleUrl)
                    .queryParam("wstoken", moodleToken)
                    .queryParam("wsfunction", "core_course_update_courses")
                    .queryParam("moodlewsrestformat", format)
                    .toUriString();

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Course section updated successfully");
                return response.getBody();
            } else {
                return createErrorResponse("Failed to update section", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("Error updating course section in Moodle", e);
            return createErrorResponse(e.getMessage(), 500);
        }
    }

    /**
     * Synchronize students and teachers from academic system to Moodle course
     * 
     * @param groupId  Group/turma ID
     * @param courseId Moodle course ID
     * @param students List of student data
     * @param teachers List of teacher data
     * @return Summary of synchronization
     */
    public Map<String, Object> synchronizeCourseEnrollments(
            String groupId,
            Integer courseId,
            List<Map<String, Object>> students,
            List<Map<String, Object>> teachers) {

        log.info("Synchronizing enrollments for group {} in course {}", groupId, courseId);

        Map<String, Object> result = new HashMap<>();
        List<String> successfulEnrollments = new ArrayList<>();
        List<String> failedEnrollments = new ArrayList<>();

        // Enroll students (role 5)
        for (Map<String, Object> student : students) {
            try {
                String username = (String) student.get("username");
                Integer userId = (Integer) student.get("userid");

                if (userId != null) {
                    enrollUser(userId, courseId, 5); // Role 5 = Student
                    successfulEnrollments.add("Student: " + username);
                }
            } catch (Exception e) {
                log.error("Failed to enroll student", e);
                failedEnrollments.add("Student: " + student.get("username"));
            }
        }

        // Enroll teachers (role 3)
        for (Map<String, Object> teacher : teachers) {
            try {
                String username = (String) teacher.get("username");
                Integer userId = (Integer) teacher.get("userid");

                if (userId != null) {
                    enrollUser(userId, courseId, 3); // Role 3 = Teacher
                    successfulEnrollments.add("Teacher: " + username);
                }
            } catch (Exception e) {
                log.error("Failed to enroll teacher", e);
                failedEnrollments.add("Teacher: " + teacher.get("username"));
            }
        }

        result.put("groupId", groupId);
        result.put("courseId", courseId);
        result.put("successfulEnrollments", successfulEnrollments);
        result.put("failedEnrollments", failedEnrollments);
        result.put("totalSuccess", successfulEnrollments.size());
        result.put("totalFailed", failedEnrollments.size());

        log.info("Synchronization completed: {} successful, {} failed",
                successfulEnrollments.size(), failedEnrollments.size());

        return result;
    }

    /**
     * Helper method to create success response
     */
    private Map<String, Object> createSuccessResponse(String message) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", message);
        return response;
    }

    /**
     * Helper method to create error response
     */
    private Map<String, Object> createErrorResponse(String message, int statusCode) {
        Map<String, Object> error = new HashMap<>();
        error.put("error", true);
        error.put("message", message);
        error.put("statusCode", statusCode);
        return error;
    }
}
