# Relatório Final — Unidade Curricular de Integração de Sistemas (Mestrado)

## 1. Introdução

Este relatório apresenta a conceção e implementação de uma solução de Integração de Aplicações Empresariais (EAI) como trabalho da unidade curricular de Integração de Sistemas do curso de mestrado. O sistema integra o módulo académico (SII), o LMS Moodle e um sistema de notificações (perspetivado), promovendo comunicação assíncrona, desacoplamento e extensibilidade através de um barramento de mensagens RabbitMQ. A orquestração é realizada por uma aplicação Spring Boot com Apache Camel, que consome, valida e transforma mensagens, invocando serviços externos via camadas de interface (REST).

Objetivos principais:

- Orquestrar fluxos entre domínio académico e Moodle, mantendo baixo acoplamento.
- Implementar padrões de integração (EIP) em cenários reais.
- Justificar escolhas tecnológicas comparando alternativas do ecossistema.
- Entregar um ambiente de teste reprodutível via Docker Compose.

---

## 2. Fundamentação Teórica (Padrões EIP)

Os Enterprise Integration Patterns (EIP) oferecem uma linguagem comum e soluções comprovadas para integrar sistemas heterogéneos. Entre os padrões aplicados e/ou considerados neste projeto:

- Message Channel: canais (filas) como `moodle.create.course.queue` e `moodle.sync.course.queue` transportam mensagens entre produtores e consumidores sem dependências diretas.
- Message: mensagens JSON padronizadas com contratos mínimos necessários (ex.: `fullname`, `shortname`, `categoryid`).
- Pipes and Filters: rotas Camel encadeiam etapas (unmarshal, validação, transformação, chamada REST), permitindo composição modular.
- Content-Based Router: encaminhamento com base no conteúdo (ex.: distinguir criação vs. sincronização de curso).
- Message Translator / Canonical Data Model: transformação de payloads do domínio académico para o formato esperado pela API do Moodle.
- Idempotent Receiver: prática recomendada para evitar efeitos duplicados (orientado e previsto para matrículas e criação de curso).
- Retry, Backoff e DLQ: reentrega exponencial e filas de quarentena planeadas para robustez (padrões referenciados; implementação futura sugerida).
- Orchestration vs. Choreography: adotado modelo de orquestração central com Camel; coreografia é considerada para serviços peer-to-peer futuros.
- Circuit Breaker e Bulkhead: padrões de resiliência previstos para proteger chamadas REST ao Moodle.

Referência: Hohpe & Woolf (Enterprise Integration Patterns) e documentação oficial de RabbitMQ, Apache Camel e Spring.

---

## 3. Justificação das Tecnologias

Escolhas baseadas em critérios de maturidade, comunidade, facilidade de operação e alinhamento com objetivos da UC.

- RabbitMQ (Message Broker)
  - Porquê: maturidade, gestão de filas/exchanges, ACKs, reentrega e UI de administração. Adequado para integração assíncrona orientada a comandos/eventos.
  - Comparativo: Kafka foca-se em streaming e retenção longa; ActiveMQ/Artemis são alternativas viáveis. Para este caso de comando/eventos e simplicidade operacional, RabbitMQ é preferível.

- Spring Boot
  - Porquê: rapidez de desenvolvimento, ecosistema Spring (AMQP, Web, Validation), configuração externa e integrações.
  - Comparativo: Micronaut/Quarkus oferecem arranque rápido; contudo, Spring domina a integração empresarial e a equipa tem maior familiaridade.

- Apache Camel
  - Porquê: DSL de rotas, componentes vastos (REST, AMQP), tratamento de erros, agregação e transformação. Facilita a aplicação de EIP.
  - Comparativo: Spring Integration é alternativa; Camel fornece uma DSL mais expressiva e uma grande biblioteca de componentes.

- Moodle (LMS)
  - Porquê: plataforma open-source amplamente usada; integrações via Web Services REST.
  - BD MySQL (no container) por compatibilidade ampla com distribuição Moodle; phpMyAdmin simplifica validação em ambiente de teste.

- Docker Compose
  - Porquê: reprodutibilidade e isolamento do ambiente (RabbitMQ, Moodle, DB, phpMyAdmin), baixando barreiras à execução e testes.

---

## 4. Arquitetura e Implementação

### 4.1 Componentes

- RabbitMQ: broker com exchange `moodle_exchange` e filas dedicadas à criação e sincronização de cursos.
- Orquestrador (Spring Boot + Camel): produtores/consumidores, rotas e serviços de integração.
- Moodle (container): Apache/PHP + Moodle; DB MySQL; administração via phpMyAdmin.

### 4.2 Topologia de Mensagens

- Exchange principal: `moodle_exchange` (direct)
- Filas:
  - `moodle.create.course.queue`
  - `moodle.sync.course.queue`
- Conjunto de exemplo interno (demo): exchange `mei-ids-exchange-example`, queue `mei-ids-queue-example`, routing-key `mei-ids-routing-key`.

### 4.3 Rotas Camel e Fluxos

- `direct:createCourse` → valida `CourseRequest` → chama `MoodleService.createCourse()` → REST.
- `direct:syncCourse` → valida `CourseSyncRequest` → orquestra atualizações e matrículas (`enrollUser`) → REST.

Código relevante:

- Rotas: [demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java](demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java)
- Consumer RabbitMQ: [demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java](demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java)
- Serviço Moodle: [demo/src/main/java/com/example/demo/services/MoodleService.java](demo/src/main/java/com/example/demo/services/MoodleService.java)
- Configuração de exemplo RabbitMQ: [demo/src/main/java/com/example/demo/config/RabbitMqExampleConfig.java](demo/src/main/java/com/example/demo/config/RabbitMqExampleConfig.java)
- Parâmetros: [demo/src/main/resources/application.properties](demo/src/main/resources/application.properties)

### 4.4 Diagrama de Alto Nível

```mermaid
flowchart LR
	A[Académico / Produtores] -- JSON --> Q1[(moodle.create.course.queue)]
	A -- JSON --> Q2[(moodle.sync.course.queue)]
	subgraph RabbitMQ
		Q1
		Q2
	end
	Q1 & Q2 --> B[Orquestrador (Spring Boot + Apache Camel)]
	B -->|REST| C[(Moodle API)]
	B -->|Futuro| D[Notificações]
```

---

## 5. Ambiente de Teste (Docker Compose)

Serviços definidos em [docker-compose.yml](docker-compose.yml):

- `rabbitmq`: UI em `http://localhost:15672` (admin/admin)
- `moodleapp`: Moodle em `http://localhost/`
- `moodledb`: MySQL 8 (db=`moodle`, user=`moodleuser`, pass=`moodlepass`)
- `phpmyadmin`: `http://localhost:8081`

Execução:

```bash
# na raiz do projeto
docker compose up -d
docker compose ps
docker compose logs -f moodleapp
```

Orquestrador (porta 7070):

```bash
cd demo
./mvnw spring-boot:run
```

Configuração (excerto):

```
app.rabbitmq.exchange=moodle_exchange
app.rabbitmq.queue.createCourse=moodle.create.course.queue
app.rabbitmq.queue.syncCourse=moodle.sync.course.queue
app.moodle.url=http://localhost/webservice/rest/server.php
app.moodle.token=<TOKEN_DO_MOODLE>
```

---

## 6. Casos de Estudo

### 6.1 Criação de Curso

Fluxo: produtor → `moodle.create.course.queue` → `RabbitMqConsumer.receive()` → `direct:createCourse` → `MoodleService.createCourse()` → Moodle.

Payload (exemplo):

```json
{
	"fullname": "Engenharia de Integração",
	"shortname": "EAI-2026",
	"categoryid": 1,
	"summary": "Criação de curso via integração"
}
```

### 6.2 Sincronização de Turma

Fluxo: produtor → `moodle.sync.course.queue` → `RabbitMqConsumer.receiveSyncCourse()` → `direct:syncCourse` → matrículas (`enrollUser`).

Payload (exemplo):

```json
{
	"groupId": "TURMA-001",
	"courseData": {
		"fullname": "EAI 2026",
		"shortname": "EAI-2026",
		"categoryid": 1,
		"summary": "Sincronização de turma"
	},
	"students": [{ "userId": 1001 }],
	"teachers": [{ "userId": 501 }]
}
```

### 6.3 Operação e Observabilidade

RabbitMQ UI para filas e taxas; logs do orquestrador para rastreabilidade; phpMyAdmin para inspeção de BD em ambiente de teste.

---

## 7. Resultados, Limitações e Trabalho Futuro

Resultados: integração funcional entre académico e Moodle, com desacoplamento via broker e orquestração Camel. Ambiente reprodutível com containers.

Limitações:

- Idempotência e DLQ não formalizados em todas as rotas.
- Token do Moodle e segurança não endurecidos para produção.
- Ausência de tracing distribuído e métricas detalhadas.

Trabalho Futuro:

- Implementar DLQs, reentrega com backoff e circuit breakers.
- Formalizar contratos (JSON Schema/Avro) e versionamento.
- TLS e gestão de segredos em produção.
- Integrar sistema de notificações (e-mail/SMS/push) e novos módulos sem impacto nos existentes.

---

## 8. Conclusões

O projeto materializa a aplicação de padrões EIP num contexto académico, demonstrando que uma EAI com RabbitMQ, Spring Boot e Apache Camel oferece desacoplamento, resiliência e extensibilidade. A solução está preparada para escalar com novos módulos de negócio e reforçar garantias de entrega e observabilidade com incrementos graduais.

---

## 9. Bibliografia / Referências

- Hohpe, G.; Woolf, B. Enterprise Integration Patterns. Addison-Wesley.
- RabbitMQ Documentation — https://www.rabbitmq.com/documentation.html
- Apache Camel — https://camel.apache.org/
- Spring Boot — https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring AMQP — https://spring.io/projects/spring-amqp
- Moodle Web Services — https://docs.moodle.org/dev/Web_services
- Docker & Compose — https://docs.docker.com/
