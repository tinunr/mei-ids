package com.example.demo.services;

import com.example.demo.bens.CourseRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
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
     * Create a course in Moodle
     * 
     * @param request CourseRequest with course details
     * @return Response from Moodle API
     */
    public Map<String, Object> createCourse(CourseRequest request) {
        log.info("Creating course in Moodle: {}", request.getFullname());

        try {
            // Build course payload
            Map<String, Object> course = new HashMap<>();
            course.put("fullname", request.getFullname());
            course.put("shortname", request.getShortname());
            course.put("categoryid", request.getCategoryid());

            if (request.getSummary() != null && !request.getSummary().isEmpty()) {
                course.put("summary", request.getSummary());
            }

            // Wrap in courses array
            Map<String, Object> payload = new HashMap<>();
            payload.put("courses", Collections.singletonList(course));

            // Build URL with query parameters
            String url = UriComponentsBuilder.fromHttpUrl(moodleUrl)
                    .queryParam("wstoken", moodleToken)
                    .queryParam("wsfunction", createFunction)
                    .queryParam("moodlewsrestformat", format)
                    .toUriString();

            // Make HTTP POST request
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(payload, headers);

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("Course created successfully: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Failed to create course. Status: {}", response.getStatusCode());
                return createErrorResponse("Failed to create course", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("Error creating course in Moodle", e);
            return createErrorResponse(e.getMessage(), 500);
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
        log.info("Enrolling user {} in course {} with role {}", userId, courseId, roleId);

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

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("User enrolled successfully");
                return response.getBody();
            } else {
                log.error("Failed to enroll user. Status: {}", response.getStatusCode());
                return createErrorResponse("Failed to enroll user", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("Error enrolling user in Moodle", e);
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
        log.info("Creating user in Moodle: {}", username);

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

            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            if (response.getStatusCode() == HttpStatus.OK) {
                log.info("User created successfully: {}", response.getBody());
                return response.getBody();
            } else {
                log.error("Failed to create user. Status: {}", response.getStatusCode());
                return createErrorResponse("Failed to create user", response.getStatusCode().value());
            }

        } catch (Exception e) {
            log.error("Error creating user in Moodle", e);
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
