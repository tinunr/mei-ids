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
