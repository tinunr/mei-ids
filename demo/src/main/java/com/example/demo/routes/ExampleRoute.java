package com.example.demo.routes;

import org.apache.camel.builder.RouteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ExampleRoute extends RouteBuilder {

    private static final Logger log = LoggerFactory.getLogger(ExampleRoute.class);

    @Override
    public void configure() throws Exception {

        onException(Exception.class)
                .handled(true)
                .log("[ExampleRoute] Erro: ${exception.message}")
                .maximumRedeliveries(0);

        /**
         * Rota que processa mensagens do exemplo
         */
        from("direct:exampleRoute")
                .routeId("example-route")
                .log("[ExampleRoute] ========================================")
                .log("[ExampleRoute] PROCESSANDO MENSAGEM DO EXEMPLO")
                .log("[ExampleRoute] Mensagem: ${body}")
                .log("[ExampleRoute] Data/Hora: ${date:now:yyyy-MM-dd HH:mm:ss}")
                .process(exchange -> {
                    String message = exchange.getIn().getBody(String.class);
                    log.info("[ExampleRoute] ✓ Mensagem processada com sucesso: {}", message);
                })
                .log("[ExampleRoute] ========================================")
                .end();
    }
}
