package com.example.demo.routes;

import com.example.demo.bens.CourseRequest;
import com.example.demo.bens.CourseSyncRequest;
import com.example.demo.services.MoodleService;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class MoodleCourseRoute extends RouteBuilder {

    @Autowired
    private MoodleService moodleService;

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("[MoodleCourseRoute] Exception: ${exception.message}")
                .setBody(constant("{'error': true, 'message': '${exception.message}'}"))
                .setHeader(Exchange.HTTP_RESPONSE_CODE, constant(500))
                .maximumRedeliveries(0);

        // Route to create a course in Moodle
        from("direct:createCourse")
                .routeId("moodle-create-course-route")
                .log("[MoodleCourseRoute] Received request: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, CourseRequest.class)
                .process(exchange -> {
                    CourseRequest request = exchange.getIn().getBody(CourseRequest.class);
                    if (request == null) {
                        throw new IllegalArgumentException("Empty CourseRequest payload");
                    }

                    // Validate required fields
                    if (request.getFullname() == null || request.getFullname().isEmpty()) {
                        throw new IllegalArgumentException("Course fullname is required");
                    }
                    if (request.getShortname() == null || request.getShortname().isEmpty()) {
                        throw new IllegalArgumentException("Course shortname is required");
                    }
                    if (request.getCategoryid() == null) {
                        throw new IllegalArgumentException("Course categoryid is required");
                    }

                    log.info("[MoodleCourseRoute] Creating course: {}", request.getFullname());

                    // Use MoodleService to create the course
                    Map<String, Object> response = moodleService.createCourse(request);

                    exchange.getMessage().setBody(response);
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
                })
                .log("[MoodleCourseRoute] Moodle response: ${body}")
                .marshal().json(JsonLibrary.Jackson);

        // Route to enroll users in a course
        from("direct:enrollUser")
                .routeId("moodle-enroll-user-route")
                .log("[MoodleCourseRoute] Enrolling user: ${body}")
                .process(exchange -> {
                    Map<String, Object> body = exchange.getIn().getBody(Map.class);

                    Integer userId = (Integer) body.get("userId");
                    Integer courseId = (Integer) body.get("courseId");
                    Integer roleId = (Integer) body.getOrDefault("roleId", 5); // Default: student

                    if (userId == null || courseId == null) {
                        throw new IllegalArgumentException("userId and courseId are required");
                    }

                    Map<String, Object> response = moodleService.enrollUser(userId, courseId, roleId);
                    exchange.getMessage().setBody(response);
                })
                .log("[MoodleCourseRoute] Enrollment response: ${body}")
                .marshal().json(JsonLibrary.Jackson);

        // Route to create a user in Moodle
        from("direct:createUser")
                .routeId("moodle-create-user-route")
                .log("[MoodleCourseRoute] Creating user: ${body}")
                .process(exchange -> {
                    Map<String, Object> body = exchange.getIn().getBody(Map.class);

                    String username = (String) body.get("username");
                    String firstName = (String) body.get("firstName");
                    String lastName = (String) body.get("lastName");
                    String email = (String) body.get("email");

                    if (username == null || firstName == null || lastName == null) {
                        throw new IllegalArgumentException("username, firstName, and lastName are required");
                    }

                    Map<String, Object> response = moodleService.createUser(username, firstName, lastName, email);
                    exchange.getMessage().setBody(response);
                })
                .log("[MoodleCourseRoute] User creation response: ${body}")
                .marshal().json(JsonLibrary.Jackson);

        // Route to search for a course
        from("direct:searchCourse")
                .routeId("moodle-search-course-route")
                .log("[MoodleCourseRoute] Searching course: ${body}")
                .process(exchange -> {
                    String courseName = exchange.getIn().getBody(String.class);

                    if (courseName == null || courseName.isEmpty()) {
                        throw new IllegalArgumentException("Course name is required");
                    }

                    Map<String, Object> response = moodleService.getCourseByName(courseName);
                    exchange.getMessage().setBody(response);
                })
                .log("[MoodleCourseRoute] Search response: ${body}")
                .marshal().json(JsonLibrary.Jackson);

        // Route to synchronize course with Moodle (complete integration)
        from("direct:syncCourse")
                .routeId("moodle-sync-course-route")
                .log("[MoodleCourseRoute] Starting course synchronization: ${body}")
                .unmarshal().json(JsonLibrary.Jackson, CourseSyncRequest.class)
                .process(exchange -> {
                    CourseSyncRequest syncRequest = exchange.getIn().getBody(CourseSyncRequest.class);

                    if (syncRequest == null || syncRequest.getGroupId() == null) {
                        throw new IllegalArgumentException("Invalid synchronization request");
                    }

                    log.info("[MoodleCourseRoute] Synchronizing group: {}", syncRequest.getGroupId());

                    Map<String, Object> syncResult = new HashMap<>();
                    syncResult.put("groupId", syncRequest.getGroupId());
                    syncResult.put("timestamp", new Date());

                    try {
                        // Step 1: Create or verify course exists
                        CourseRequest courseData = syncRequest.getCourseData();
                        if (courseData != null) {
                            log.info("[MoodleCourseRoute] Creating course: {}", courseData.getFullname());
                            Map<String, Object> courseResponse = moodleService.createCourse(courseData);
                            syncResult.put("courseCreation", courseResponse);

                            // Extract course ID from response if available
                            Integer courseId = extractCourseId(courseResponse);
                            syncResult.put("courseId", courseId);

                            // Step 2: Create users (students and teachers) if they don't exist
                            List<Map<String, Object>> createdUsers = new ArrayList<>();

                            // Create students
                            if (syncRequest.getStudents() != null) {
                                log.info("[MoodleCourseRoute] Creating {} students", syncRequest.getStudents().size());
                                for (CourseSyncRequest.StudentData student : syncRequest.getStudents()) {
                                    try {
                                        Map<String, Object> userResponse = moodleService.createUser(
                                                student.getUsername(),
                                                student.getName(),
                                                student.getShortname() != null ? student.getShortname() : "",
                                                student.getEmail());
                                        createdUsers.add(Map.of(
                                                "username", student.getUsername(),
                                                "type", "student",
                                                "response", userResponse));
                                    } catch (Exception e) {
                                        log.warn("[MoodleCourseRoute] Failed to create student {}: {}",
                                                student.getUsername(), e.getMessage());
                                    }
                                }
                            }

                            // Create teachers
                            if (syncRequest.getTeachers() != null) {
                                log.info("[MoodleCourseRoute] Creating {} teachers", syncRequest.getTeachers().size());
                                for (CourseSyncRequest.TeacherData teacher : syncRequest.getTeachers()) {
                                    try {
                                        Map<String, Object> userResponse = moodleService.createUser(
                                                teacher.getUsername(),
                                                teacher.getName(),
                                                teacher.getShortname() != null ? teacher.getShortname() : "",
                                                teacher.getEmail());
                                        createdUsers.add(Map.of(
                                                "username", teacher.getUsername(),
                                                "type", "teacher",
                                                "response", userResponse));
                                    } catch (Exception e) {
                                        log.warn("[MoodleCourseRoute] Failed to create teacher {}: {}",
                                                teacher.getUsername(), e.getMessage());
                                    }
                                }
                            }

                            syncResult.put("usersCreated", createdUsers);

                            // Step 3: Enroll users in the course
                            if (courseId != null) {
                                List<Map<String, Object>> enrollments = new ArrayList<>();

                                // Enroll students (role 5)
                                if (syncRequest.getStudents() != null) {
                                    log.info("[MoodleCourseRoute] Enrolling students");
                                    List<Map<String, Object>> studentMaps = syncRequest.getStudents().stream()
                                            .map(s -> Map.of(
                                                    "username", (Object) s.getUsername(),
                                                    "userid", (Object) Integer.parseInt(s.getPersonId())))
                                            .collect(Collectors.toList());

                                    for (Map<String, Object> student : studentMaps) {
                                        try {
                                            Integer userId = (Integer) student.get("userid");
                                            Map<String, Object> enrollResponse = moodleService.enrollUser(
                                                    userId, courseId, 5);
                                            enrollments.add(Map.of(
                                                    "username", student.get("username"),
                                                    "role", "student",
                                                    "success", true));
                                        } catch (Exception e) {
                                            log.warn("[MoodleCourseRoute] Failed to enroll student: {}",
                                                    e.getMessage());
                                            enrollments.add(Map.of(
                                                    "username", student.get("username"),
                                                    "role", "student",
                                                    "success", false,
                                                    "error", e.getMessage()));
                                        }
                                    }
                                }

                                // Enroll teachers (role 3)
                                if (syncRequest.getTeachers() != null) {
                                    log.info("[MoodleCourseRoute] Enrolling teachers");
                                    List<Map<String, Object>> teacherMaps = syncRequest.getTeachers().stream()
                                            .map(t -> Map.of(
                                                    "username", (Object) t.getUsername(),
                                                    "userid", (Object) Integer.parseInt(t.getPersonId())))
                                            .collect(Collectors.toList());

                                    for (Map<String, Object> teacher : teacherMaps) {
                                        try {
                                            Integer userId = (Integer) teacher.get("userid");
                                            Map<String, Object> enrollResponse = moodleService.enrollUser(
                                                    userId, courseId, 3);
                                            enrollments.add(Map.of(
                                                    "username", teacher.get("username"),
                                                    "role", "teacher",
                                                    "success", true));
                                        } catch (Exception e) {
                                            log.warn("[MoodleCourseRoute] Failed to enroll teacher: {}",
                                                    e.getMessage());
                                            enrollments.add(Map.of(
                                                    "username", teacher.get("username"),
                                                    "role", "teacher",
                                                    "success", false,
                                                    "error", e.getMessage()));
                                        }
                                    }
                                }

                                syncResult.put("enrollments", enrollments);

                                // Count successes
                                long successCount = enrollments.stream()
                                        .filter(e -> (Boolean) e.getOrDefault("success", false))
                                        .count();
                                syncResult.put("enrollmentSuccess", successCount);
                                syncResult.put("enrollmentFailed", enrollments.size() - successCount);
                            }

                            // Step 4: Update course sections with content
                            if (courseId != null && syncRequest.getObjectives() != null) {
                                log.info("[MoodleCourseRoute] Updating course sections");
                                String summary = buildCourseSummary(syncRequest);
                                Map<String, Object> sectionResponse = moodleService.updateCourseSection(
                                        courseId, 0, summary);
                                syncResult.put("sectionUpdate", sectionResponse);
                            }

                            syncResult.put("success", true);
                            syncResult.put("message", "Course synchronized successfully");

                        } else {
                            throw new IllegalArgumentException("Course data is required for synchronization");
                        }

                    } catch (Exception e) {
                        log.error("[MoodleCourseRoute] Synchronization failed: {}", e.getMessage(), e);
                        syncResult.put("success", false);
                        syncResult.put("error", e.getMessage());
                    }

                    exchange.getMessage().setBody(syncResult);
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
                })
                .log("[MoodleCourseRoute] Synchronization completed: ${body}")
                .marshal().json(JsonLibrary.Jackson);
    }

    /**
     * Extract course ID from Moodle response
     */
    private Integer extractCourseId(Map<String, Object> response) {
        if (response != null && response.containsKey("id")) {
            return (Integer) response.get("id");
        }
        // Try to extract from courses array
        if (response != null && response.containsKey("courses")) {
            List courses = (List) response.get("courses");
            if (!courses.isEmpty()) {
                Map course = (Map) courses.get(0);
                return (Integer) course.get("id");
            }
        }
        return null;
    }

    /**
     * Build course summary from sync request data
     */
    private String buildCourseSummary(CourseSyncRequest syncRequest) {
        StringBuilder summary = new StringBuilder();

        if (syncRequest.getObjectives() != null) {
            summary.append("<h3>Objetivos</h3>\n");
            summary.append("<p>").append(syncRequest.getObjectives()).append("</p>\n");
        }

        if (syncRequest.getContent() != null) {
            summary.append("<h3>Conteúdo</h3>\n");
            summary.append("<p>").append(syncRequest.getContent()).append("</p>\n");
        }

        if (syncRequest.getMethodology() != null) {
            summary.append("<h3>Metodologia</h3>\n");
            summary.append("<p>").append(syncRequest.getMethodology()).append("</p>\n");
        }

        if (syncRequest.getEvaluation() != null) {
            summary.append("<h3>Avaliação</h3>\n");
            summary.append("<p>").append(syncRequest.getEvaluation()).append("</p>\n");
        }

        if (syncRequest.getBibliographyDescription() != null) {
            summary.append("<h3>Bibliografia</h3>\n");
            summary.append("<p>").append(syncRequest.getBibliographyDescription()).append("</p>\n");
        }

        return summary.toString();
    }
}
