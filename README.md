# Relatório Final — Unidade Curricular de Integração de Sistemas (Mestrado)

## 1. Introdução

Este relatório apresenta a conceção e implementação de uma solução de Integração de Aplicações Empresariais (EAI) como trabalho da unidade curricular de Integração de Sistemas do curso de mestrado. O sistema integra o módulo académico (SII), o LMS Moodle e um sistema de notificações (perspetivado), promovendo comunicação assíncrona, desacoplamento e extensibilidade através de um barramento de mensagens RabbitMQ. A orquestração é realizada por uma aplicação Spring Boot com Apache Camel, que consome, valida e transforma mensagens, invocando serviços externos via camadas de interface (REST).

Objetivos principais:

- Orquestrar fluxos entre domínio académico e Moodle, mantendo baixo acoplamento.
- Implementar padrões de integração (EIP) em cenários reais.
- Justificar escolhas tecnológicas comparando alternativas do ecossistema.
- Entregar um ambiente de teste reprodutível via Docker Compose.

---

## 2. Fundamentação Teórica (Modelos de Integração EAI)

A Integração de Aplicações Empresariais (EAI) estrutura-se em modelos arquiteturais que definem como sistemas heterogéneos comunicam e colaboram. Os principais modelos e sua aplicação neste projeto:

- Point-to-Point: ligações diretas entre aplicações. Simples, porém de alto acoplamento e difícil escalabilidade; não adotado como abordagem principal.
- Hub-and-Spoke (ESB): um barramento central normaliza mensagens e aplica mediação. Potente, mas pode introduzir complexidade e dependência do ESB.
- Message Broker: broker de mensagens (RabbitMQ) intermedia produtores e consumidores com filas e exchanges, promovendo desacoplamento, backpressure e resiliência. É a base escolhida para comunicação assíncrona.
- Mediator/Orchestrator: um componente central (Apache Camel no Spring Boot) coordena fluxos, valida e transforma mensagens, chamando serviços externos (REST do Moodle). Este papel materializa a orquestração de processos.
- SOA/Microservices: serviços autónomos expõem interfaces estáveis; a integração usa contratos bem definidos e mensageria. A solução segue princípios SOA, mantendo interfaces REST e contratos JSON.

Modelo adotado: Message Broker (RabbitMQ) + Orquestrador (Camel) — o broker assegura desacoplamento e entrega confiável; o orquestrador aplica regras de negócio, validações e transformações, reduzindo acoplamento entre domínios.

Boas práticas relacionadas ao modelo EAI:

- Contratos canónicos (JSON) e validação de esquema.
- Idempotência para evitar duplicações em reprocessamentos.
- Reentrega com backoff e DLQ para mensagens problemáticas.
- Observabilidade e rastreabilidade dos fluxos.

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
app.moodle.url=http:/localhost/webservice/rest/server.php
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

A arquitetura escolhida foi concebida com foco estratégico na observabilidade futura, permitindo escalabilidade operacional e visibilidade completa dos fluxos de integração. Esta abordagem é um dos pilares fundamentais da solução.

**Observabilidade Atual (Implementada):**

- **RabbitMQ Management UI** (`http://localhost:15672`): fornece visibilidade em tempo real sobre:
  - Taxa de mensagens (ingresso/egresso por fila)
  - Comprimento de filas e latência de processamento
  - Retenção de mensagens e estado de conexões
  - Análise rápida de gargalos ou acumulação de mensagens

- **Logs Estruturados do Orquestrador (Camel)**: 
  - Cada rota Camel regista eventos-chave (processamento, validação, erros)
  - Rastreabilidade de transações desde entrada em fila até resultado em serviço alvo
  - Identificação de falhas e análise de impacto

- **phpMyAdmin**: acesso rápido à base de dados MySQL para inspeção de estado do Moodle em ambiente de teste.

**Observabilidade Futura (Extensível):**

A arquitetura foi projetada como base sólida para implementar:

1. **Distributed Tracing (OpenTelemetry/Jaeger)**:
   - Correlação de mensagens através de `trace-id` e `span-id` propagados em headers RabbitMQ
   - Visibilidade de latência em cada componente (broker → orquestrador → Moodle)
   - Identificação de caminhos críticos e otimização

2. **Métricas Detalhadas (Prometheus/Micrometer)**:
   - Métricas nativas do RabbitMQ (throughput, latência de ACK, rejeições)
   - Métricas do orquestrador (rotas Camel, processadores, serviços REST)
   - Alertas automáticos em caso de degeneração de desempenho

3. **Centralização de Logs (ELK Stack/Loki)**:
   - Agregação de logs de todos os componentes (RabbitMQ, Orquestrador, Moodle)
   - Pesquisa e análise histórica de incidentes
   - Dashboards customizáveis para diferentes stakeholders

4. **Dead-Letter Queues (DLQ) com Observação**:
   - Mensagens que falham são automaticamente encaminhadas para DLQ
   - Monitorização de taxa de falhas e alertas proativos
   - Replay controlado após correção de problemas

5. **Circuit Breakers e Health Checks**:
   - Monitorização da saúde de integração com Moodle (REST availability)
   - Padrão de circuit breaker (Hystrix/Resilience4j) integrado em Camel
   - Estado visível em dashboards de operação

**Razões Arquiteturais para Esta Abordagem:**

- **Desacoplamento + Observabilidade**: RabbitMQ separa produtores de consumidores, permitindo adicionar observadores (monitorização) sem alterar lógica de negócio
- **Rastreabilidade Assíncrona**: cada mensagem pode ser correlacionada através de IDs únicos, essencial para sistemas distribuídos
- **Escalabilidade Operacional**: à medida que novos módulos são integrados (notificações, ERPs), a infraestrutura de observação cresce proporcionalmente
- **Conformidade e Auditoria**: registro completo de transformações de dados em fluxos académicos

**Exemplo de Evolução:**

```
Hoje:                        Futuro:
Logs locais            →      ELK Stack
RabbitMQ UI            →      Prometheus + Grafana
Manual troubleshooting →      Jaeger + Distributed Tracing
                              + Alertas automáticos
```

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

O projeto materializa a aplicação de modelos EAI num contexto académico, demonstrando que uma combinação de Message Broker (RabbitMQ) e Orquestrador (Spring Boot + Apache Camel) oferece desacoplamento, resiliência e extensibilidade. A solução está preparada para escalar com novos módulos de negócio e reforçar garantias de entrega e observabilidade com incrementos graduais.

---

## 9. Bibliografia / Referências

- Hohpe, G.; Woolf, B. Enterprise Integration Patterns. Addison-Wesley.
- RabbitMQ Documentation — https://www.rabbitmq.com/documentation.html
- Apache Camel — https://camel.apache.org/
- Spring Boot — https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring AMQP — https://spring.io/projects/spring-amqp
- Moodle Web Services — https://docs.moodle.org/dev/Web_services
- Docker & Compose — https://docs.docker.com/

# MEI-IDS — Relatório Final do Projeto de Integração de Sistemas (EAI)

## 1. Introdução

Este projeto implementa uma solução de Integração de Aplicações Empresariais (EAI) para orquestrar mensagens entre três domínios principais: módulo académico (SII/Académico), LMS Moodle e sistema de notificação. A comunicação assíncrona é realizada via RabbitMQ (message broker), enquanto o orquestrador é desenvolvido em Spring Boot com Apache Camel, responsável por consumir mensagens, aplicar validações/regras e invocar serviços de interface (REST do Moodle e, futuramente, outros módulos).

O objetivo é fornecer uma espinha dorsal de integração extensível, resiliente e observável, que permita adicionar novos módulos de negócio sem alterar os componentes existentes, seguindo boas práticas de EAI.

---

## 2. Enquadramento e Objetivos

- Estabelecer um barramento de mensagens baseado em RabbitMQ para desacoplar produtores e consumidores.
- Implementar um orquestrador (Spring Boot + Apache Camel) que consome, valida e transforma mensagens, encaminhando-as para serviços alvo.
- Integrar o Moodle via API REST para casos de uso académicos (ex.: criação de cursos, inscrição de utilizadores, sincronização de turmas).
- Disponibilizar ambiente de teste containerizado (Docker Compose) com RabbitMQ, Moodle, MySQL e phpMyAdmin.
- Preparar a arquitetura para integração futura com novos módulos (ex.: notificações, ERPs, CRMs) mantendo baixo acoplamento.

---

## 3. Arquitetura da Solução

### 3.1 Componentes

- RabbitMQ: message broker, disponibiliza exchange e filas para comunicação assíncrona.
- Orquestrador EAI: aplicação Spring Boot com Apache Camel; contém produtores/consumidores, rotas, validações e serviços de acesso a sistemas externos.
- Moodle (container): LMS integrado via Web Services REST; base de dados MySQL; administração via phpMyAdmin.

### 3.2 Topologia de Mensagens

- Exchange (aplicação): `moodle_exchange` (direct)
- Filas principais:
  - `moodle.create.course.queue` — criação de curso no Moodle
  - `moodle.sync.course.queue` — sincronização de curso (ex.: turmas, docentes, discentes)
- Conjunto de exemplo (demo interno):
  - Exchange: `mei-ids-exchange-example`
  - Queue: `mei-ids-queue-example`
  - RoutingKey: `mei-ids-routing-key`

### 3.3 Rotas Camel e Serviços

- `direct:createCourse` → valida payload (`CourseRequest`) → `MoodleService.createCourse()` → Moodle REST.
- `direct:syncCourse` → valida/transforma (`CourseSyncRequest`) → orchestration (enrolamentos, atualizações) → Moodle REST.
- Endpoints utilitários expostos por `RabbitMqTestController` para testes de conectividade e envio de mensagens.

### 3.4 Diagrama (alto nível)

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

## 4. Desenvolvimento Teórico

### 4.1 EAI e Padrões de Integração

- Message Broker: separa emissores de recetores, promove escalabilidade e resiliência.
- Content-Based Routing: decisões de encaminhamento baseadas no conteúdo da mensagem.
- Orquestração vs. Coreografia: aqui adotamos orquestração central (Camel) para coordenar fluxos.
- Idempotência e Reentrega: fundamentais para evitar efeitos colaterais em reprocessamentos.
- Dead-Letter Queues (DLQ): recomendadas para mensagens que falham repetidamente (planeado como melhoria).

### 4.2 RabbitMQ (Exchange/Queue/Binding)

- Exchanges distribuem mensagens para filas via bindings e routing-keys.
- Filas persistentes e acknowledgements garantem confiabilidade.
- Management UI exposta em `http://localhost:15672` (user: admin, pass: admin).

### 4.3 Apache Camel

- Rotas (`RouteBuilder`) definem pipelines de processamento (unmarshal JSON, validação, processors, chamadas REST).
- Endpoints internos `direct:*` permitem compor fluxos (ex.: `direct:createCourse`, `direct:syncCourse`).
- Tratamento de exceções e enriquecimento de mensagens centralizados.

### 4.4 Spring Boot + Spring AMQP

- Integração com RabbitMQ via `@RabbitListener` para consumo e `RabbitTemplate` para publicação.
- Externalização de configurações em `application.properties`.

---

## 5. Desenvolvimento Prático

### 5.1 Repositório e Estrutura

- Orquestrador: `demo/` (Spring Boot + Camel)
- Contêineres: `docker-compose.yml` (raiz), `moodle/`, `rabbitmq/`

### 5.2 Serviços Docker (ambiente de teste)

`docker-compose.yml` (raiz) disponibiliza:

- `rabbitmq` — broker com UI em 15672
- `moodleapp` — Apache/PHP + Moodle, porta 80
- `moodledb` — MySQL 8
- `phpmyadmin` — UI de BD em 8081

Credenciais de teste (fixas, apenas para ambiente dev):

- MySQL: db=`moodle`, user=`moodleuser`, pass=`moodlepass`
- RabbitMQ: user=`admin`, pass=`admin`

Comandos:

```bash
# na raiz do projeto
docker compose up -d
docker compose ps
docker compose logs -f moodleapp
```

### 5.3 Aplicação (Spring Boot)

Requisitos: JDK 17+.

```bash
cd demo
./mvnw spring-boot:run
# App em http://localhost:7070
```

Parâmetros relevantes (demo/src/main/resources/application.properties):

```
app.rabbitmq.exchange=moodle_exchange
app.rabbitmq.queue.createCourse=moodle.create.course.queue
app.rabbitmq.queue.syncCourse=moodle.sync.course.queue
app.moodle.url=http:/localhost/webservice/rest/server.php
app.moodle.token=<TOKEN_DO_MOODLE>
```

### 5.4 Fluxos Implementados

1. Criação de Curso no Moodle

- Produtor publica JSON em `moodle.create.course.queue`.
- `RabbitMqConsumer.receive()` consome e envia para `direct:createCourse`.
- `MoodleCourseRoute` valida e invoca `MoodleService.createCourse()` → Moodle REST.

Payload (exemplo mínimo):

```json
{
	"fullname": "Engenharia de Integração",
	"shortname": "EAI-2026",
	"categoryid": 1,
	"summary": "Criação de curso via integração"
}
```

2. Sincronização de Curso

- Produtor publica JSON em `moodle.sync.course.queue`.
- `RabbitMqConsumer.receiveSyncCourse()` → `direct:syncCourse`.
- Camel orquestra inscrições (ex.: `enrollUser`) e atualizações de curso.

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

3. Endpoints de Teste (HTTP)
   `RabbitMqTestController` expõe endpoints para enviar mensagens de teste:

- `GET /api/rabbitmq/info`
- `GET /api/rabbitmq/test` (connectividade básica)
- `POST /api/rabbitmq/health-check`
- `POST /api/rabbitmq/test-sync-course`
- `POST /api/rabbitmq/test-create-course`

---

## 6. Casos de Estudo

### Caso 1 — Criação de Curso a partir do Módulo Académico

- Gatilho: evento académico (novo curso) → publicação em `moodle.create.course.queue`.
- Orquestração: validações obrigatórias (`fullname`, `shortname`, `categoryid`), enriquecimento opcional (`summary`).
- Ação: chamada ao Moodle (REST) → criação e retorno do identificador do curso.
- Métrica: tempo de resposta do Moodle; registo de sucesso/erro no orquestrador.

### Caso 2 — Sincronização de Turma (matrículas)

- Gatilho: evento de sincronização no SII → publicação em `moodle.sync.course.queue`.
- Orquestração: carregamento de `students` e `teachers`, idempotência na inscrição.
- Ação: `MoodleService.enrollUser()` (role estudante=5, docente=3).
- Resultado: consistência entre SII e Moodle.

### Caso 3 — Operação e Observabilidade

- RabbitMQ UI para inspeção de filas, taxa de mensagens e reentregas.
- Logs centralizados do orquestrador (nível INFO/ERROR) para rastreabilidade.
- phpMyAdmin para verificação rápida da base do Moodle em ambiente de teste.

---

## 7. Boas Práticas e Segurança

- Separação de ambientes (dev/test/prod) com configurações específicas.
- Manter credenciais reais fora do repositório (no teste, credenciais são fixas apenas para facilitar validação local).
- Considerar TLS para RabbitMQ e tokens seguros para o Moodle em produção.
- Implementar DLQ e políticas de reentrega exponencial para robustez.
- Observabilidade: métricas, tracing distribuído e correlação de mensagens.

---

## 8. Conclusões e Trabalho Futuro

O projeto atingiu o objetivo de disponibilizar uma EAI funcional que orquestra mensagens entre módulo académico, Moodle e (futuramente) sistema de notificações, com ênfase em desacoplamento, extensibilidade e operação em ambiente containerizado.

Próximos passos sugeridos:

- Introduzir DLQs e circuit breakers nas rotas críticas.
- Normalizar contratos de mensagens (ex.: Avro/JSON Schema) e versionamento.
- Autenticação forte e TLS em broker e serviços externos.
- Adicionar novos módulos (ex.: notificações omnicanal, relatórios, ERPs) sem impacto nos existentes.

---

## 9. Como Executar (Resumo)

```bash
# 1) Subir containers (raiz)
docker compose up -d

# 2) Arrancar orquestrador (porta 7070)
cd demo
./mvnw spring-boot:run

# 3) Testes rápidos de RabbitMQ
curl http://localhost:7070/api/rabbitmq/info
```

---

## 10. Bibliografia / Referências

- Hohpe, G.; Woolf, B. Enterprise Integration Patterns.
- RabbitMQ Documentation — https://www.rabbitmq.com/documentation.html
- Apache Camel — https://camel.apache.org/
- Spring Boot — https://docs.spring.io/spring-boot/docs/current/reference/html/
- Spring AMQP — https://spring.io/projects/spring-amqp
- Moodle Web Services — https://docs.moodle.org/dev/Web_services
- Docker & Compose — https://docs.docker.com/
