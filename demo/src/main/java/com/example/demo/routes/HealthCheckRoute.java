package com.example.demo.routes;

import org.apache.camel.builder.RouteBuilder;
import org.springframework.stereotype.Component;

@Component
public class HealthCheckRoute extends RouteBuilder {

    @Override
    public void configure() throws Exception {
        from("timer:health?period=5000")
                .routeId("health-check-route")
                .log("[HealthCheckRoute] application is alive at ${date:now:yyyy-MM-dd HH:mm:ss}")
                .end();
    }
}
