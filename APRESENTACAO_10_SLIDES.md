# MEI-IDS — Apresentação do Projeto
## 10 Slides para PowerPoint

---

## SLIDE 1: Título e Contexto

**MEI-IDS: Integração de Aplicações Empresariais (EAI)**

**Unidade Curricular:** Integração de Sistemas (Mestrado)

**Objetivo Principal:**
- Orquestrar fluxos entre módulo académico (SII), LMS Moodle e sistema de notificações
- Implementar padrões de integração (EIP) em cenários reais
- Arquitetura extensível, resiliente e observável

**Tecnologias Chave:**
- RabbitMQ (Message Broker)
- Spring Boot + Apache Camel (Orquestrador)
- Moodle (LMS)
- Docker Compose (Ambiente de teste)

---

## SLIDE 2: O Problema

**Cenário Inicial:**

❌ **Integração Ponto-a-Ponto (Point-to-Point)**
- Alto acoplamento entre sistemas
- Difícil manutenção e escalabilidade
- Sem garantias de entrega

❌ **Desafios Académicos:**
- Sincronização manual de cursos e matrículas
- Inconsistência entre SII e Moodle
- Sem rastreabilidade de operações

✅ **Solução:** Arquitetura EAI com Message Broker

---

## SLIDE 3: Arquitetura da Solução

```
┌─────────────────┐
│  Módulo Académico│──┐
└─────────────────┘  │
                      │   JSON
┌─────────────────┐  │
│   Notificações  │──┤────→ RabbitMQ Exchange
└─────────────────┘  │      ↓
                      │   Filas
┌─────────────────┐  │
│    Produtores   │──┘      (create/sync)
└─────────────────┘         ↓
                         Orquestrador
                      (Spring Boot + Camel)
                             ↓
                         REST API
                             ↓
                         Moodle
```

**Componentes Principais:**
1. RabbitMQ (Message Broker)
2. Spring Boot + Apache Camel (Orquestrador)
3. Moodle + MySQL (LMS)
4. Containers Docker (Reprodutibilidade)

---

## SLIDE 4: Topologia de Mensagens

**Exchange Principal:** `moodle_exchange` (tipo Direct)

**Filas Implementadas:**

1. **`moodle.create.course.queue`**
   - Criar novo curso no Moodle
   - Payload: CourseRequest (fullname, shortname, categoryid)

2. **`moodle.sync.course.queue`**
   - Sincronizar turma (estudantes + professores)
   - Payload: CourseSyncRequest (groupId, courseData, students, teachers)

3. **`mei-ids-queue-example`** (Demo)
   - Exemplo funcional completo para testes

**Routing Key:** correspondente ao nome da fila

---

## SLIDE 5: Rotas Camel (Orquestração)

**Rota 1: Criação de Curso**

```
direct:createCourse
  ↓ Unmarshal JSON
  ↓ Validar CourseRequest
  ↓ MoodleService.createCourse()
  ↓ REST → Moodle API
  ↓ Marshal JSON + retorno
```CC

**Rota 2: Sincronização de Turma**

```
direct:syncCourse
  ↓ Unmarshal JSON
  ↓ Validar CourseSyncRequest
  ↓ Criar usuários (se não existem)
  ↓ Inscrever estudantes (role=5)C
  ↓ Inscrever professores (role=3)
  ↓ Atualizar seções do curso
  ↓ Marshal JSON + resumo de operações
```

**Tratamento de Erros:** OnException centralizado com retry e logging

---

## SLIDE 6: MoodleService (Integração REST)

**Métodos Principais:**

| Método | Descrição |
|--------|-----------|
| `createCourse()` | Cria curso no Moodle |
| `createUser()` | Cria utilizador (role db) |
| `enrollUser()` | Inscreve em curso (role 5=estudante, 3=professor) |
| `getCourseByName()` | Busca curso por nome |
| `updateCourseSection()` | Atualiza seções com conteúdo |
| `synchronizeCourseEnrollments()` | Orquestra matrículas em massa |

**Características:**
- HttpClient: RestTemplate (Spring)
- Autenticação: Moodle token (config externe)
- Respostas: JSON estruturado com logs SLF4J

---

## SLIDE 7: Ambiente de Teste (Docker Compose)

**Serviços Containerizados:**

| Serviço | Porta | Função |
|---------|-------|--------|
| **RabbitMQ** | 15672 | Message Broker + Management UI |
| **Moodle** | 80 | LMS (Apache/PHP) |
| **MySQL** | 3306 | Base de dados Moodle |
| **phpMyAdmin** | 8081 | UI para gestão de BD |

**Inicialização:**

```bash
# Subir containers
docker compose up -d

# Verificar status
docker compose ps

# Logs
docker compose logs -f moodleapp
```

**Credenciais Teste:**
- RabbitMQ: admin/admin
- MySQL: moodleuser/moodlepass
- Moodle: admin/admin (criar após inicialização)

---

## SLIDE 8: Operação e Observabilidade

**Observabilidade ATUAL (Implementada):**

✅ **RabbitMQ Management UI** (`http://localhost:15672`)
  - Taxa de mensagens (ingresso/egresso)
  - Comprimento de filas
  - Análise de gargalos

✅ **Logs Estruturados (SLF4J)**
  - Rastreabilidade desde fila até Moodle
  - Eventos-chave: validação, processamento, erros

✅ **phpMyAdmin**
  - Inspeção de BD Moodle

**Observabilidade FUTURA (Extensível):**

🔮 **Distributed Tracing** (OpenTelemetry/Jaeger)
  - Correlação com trace-id/span-id

🔮 **Métricas Detalhadas** (Prometheus/Micrometer)
  - Throughput, latência, alertas

🔮 **Centralização de Logs** (ELK Stack/Loki)
  - Agregação e dashboards

🔮 **Dead-Letter Queues (DLQ)**
  - Reentrega e replay de falhas

🔮 **Circuit Breakers**
  - Resiliência com Hystrix/Resilience4j

---

## SLIDE 9: Casos de Estudo — Fluxos em Ação

**Caso 1: Criação de Curso**

1. SII publica JSON em `moodle.create.course.queue`
2. Consumer consome → `direct:createCourse`
3. Validação + enriquecimento
4. REST → Moodle API
5. ✓ Curso criado com ID retornado

**Caso 2: Sincronização de Turma**

1. SII publica JSON em `moodle.sync.course.queue`
2. Consumer consome → `direct:syncCourse`
3. Criar 3 estudantes + 2 professores
4. Inscrever com roles corretos
5. ✓ Turma sincronizada, log de sucesso/falha

**Caso 3: Teste via REST API**

- `GET /api/example/test` → Envia "Ola estou aqui" para RabbitMQ
- Fluxo completo: REST → Producer → RabbitMQ → Consumer → Camel Route → Log

---

## SLIDE 10: Conclusões e Trabalho Futuro

**O Que Conseguimos:**

✅ Arquitetura EAI funcional e reprodutível
✅ Desacoplamento via Message Broker (RabbitMQ)
✅ Orquestração de fluxos complexos (Apache Camel)
✅ Integração REST com Moodle
✅ Ambiente containerizado (Docker Compose)
✅ Preparado para extensão com novos módulos

**Limitações Atuais:**

⚠️ Idempotência não formalizada em todas as rotas
⚠️ DLQ ainda em plano
⚠️ Segurança (tokens/TLS) não endurecida para produção
⚠️ Sem tracing distribuído centralizado

**Próximos Passos (Roadmap):**

1. Implementar DLQ + reentrega com backoff exponencial
2. Circuit breakers para Moodle REST
3. Distributed Tracing com Jaeger
4. Métricas com Prometheus + Grafana
5. Novos módulos (notificações, ERPs) sem impacto
6. TLS e gestão de segredos (Vault)

**Conclusão Final:**

*"Uma solução EAI resiliente, extensível e pronta para escalar com novos domínios de negócio."*
