package com.example.demo.routes;

import com.example.demo.bens.CourseRequest;
import org.apache.camel.Exchange;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.model.dataformat.JsonLibrary;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Component
public class MoodleCourseRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("[MoodleCourseRoute] Exception: ${exception.message}")
                .maximumRedeliveries(0);

        from("direct:createCourse")
                .routeId("moodle-create-course-route")
                .unmarshal().json(JsonLibrary.Jackson, CourseRequest.class)
                .process(exchange -> {
                    CourseRequest req = exchange.getIn().getBody(CourseRequest.class);
                    if (req == null) {
                        throw new IllegalArgumentException("Empty CourseRequest payload");
                    }
                    // Build Moodle payload: courses => [ { ... } ]
                    Map<String, Object> course = new HashMap<>();
                    course.put("fullname", req.getFullname());
                    course.put("shortname", req.getShortname());
                    course.put("categoryid", req.getCategoryid());
                    course.put("summary", req.getSummary());

                    Map<String, Object> body = new HashMap<>();
                    body.put("courses", Collections.singletonList(course));

                    exchange.getMessage().setBody(body);
                    exchange.getMessage().setHeader(Exchange.CONTENT_TYPE, "application/json");
                })
                .log("[MoodleCourseRoute] Posting to Moodle: ${body}")
                .toD("{{app.moodle.url}}?wstoken={{app.moodle.token}}&wsfunction={{app.moodle.create-function}}&moodlewsrestformat={{app.moodle.format}}")
                .log("[MoodleCourseRoute] Moodle response: ${body}");
    }
}
