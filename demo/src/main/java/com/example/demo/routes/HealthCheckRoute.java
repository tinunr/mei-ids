package com.example.demo.routes;

import com.example.demo.producer.MessageProducer;
import org.apache.camel.builder.RouteBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class HealthCheckRoute extends RouteBuilder {

    @Autowired
    private MessageProducer messageProducer;

    @Override
    public void configure() throws Exception {
        from("timer:health?period=9000")
                .routeId("health-check-route")
                .log("[HealthCheckRoute] application is alive at ${date:now:yyyy-MM-dd HH:mm:ss}")
                .process(exchange -> {
                    String timestamp = LocalDateTime.now().format(DateTimeFormatter.ISO_DATE_TIME);

                    // Enviar notificação de email com informação de health check
                    messageProducer.sendEmailNotification(
                            "tinu5nr@gmail.com",
                            "Health Check - Aplicação Ativa",
                            "<p>A aplicação está funcionando corretamente.</p><p>Timestamp: " + timestamp + "</p>",
                            "A aplicação está funcionando corretamente. Timestamp: " + timestamp);
                })
                .end();
    }
}
