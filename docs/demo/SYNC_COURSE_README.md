# Sincronização de Cursos com Moodle via RabbitMQ

## 📋 Visão Geral

Este módulo implementa a sincronização completa de cursos do sistema acadêmico para o Moodle via RabbitMQ.

## 🔄 Funcionalidades

A rota de sincronização (`direct:syncCourse`) realiza as seguintes operações:

1. **Criação do Curso** - Cria o curso no Moodle com todas as informações
2. **Criação de Usuários** - Cria estudantes e professores no Moodle
3. **Inscrições** - Inscreve automaticamente:
   - Estudantes com role 5 (Student)
   - Professores com role 3 (Teacher)
4. **Atualização de Conteúdo** - Atualiza as seções do curso com:
   - Objetivos
   - Conteúdo programático
   - Metodologia
   - Avaliação
   - Bibliografia

## 📡 Configuração do RabbitMQ

### application.properties

```properties
# RabbitMQ
spring.rabbitmq.host=localhost
spring.rabbitmq.port=5672
spring.rabbitmq.username=admin
spring.rabbitmq.password=admin
app.rabbitmq.exchange=moodle_exchange
app.rabbitmq.queue.syncCourse=moodle.sync.course.queue
```

### Criar a Fila no RabbitMQ

```bash
# Via Management Console (http://localhost:15672)
# Ou via CLI:
rabbitmqadmin declare queue name=moodle.sync.course.queue durable=true

# Bind da fila ao exchange:
rabbitmqadmin declare binding source=moodle_exchange destination=moodle.sync.course.queue routing_key=moodle.sync.course.queue
```

## 📨 Formato da Mensagem

Envie uma mensagem JSON para a fila `moodle.sync.course.queue`:

```json
{
  "groupId": "1234",
  "courseData": {
    "fullname": "Programação Java - Turma 2026",
    "shortname": "JAVA2026",
    "categoryid": 1,
    "summary": "Curso de Programação Java"
  },
  "objectives": "Aprender POO com Java",
  "content": "1. Introdução\n2. Variáveis\n3. POO",
  "methodology": "Aulas práticas e teóricas",
  "evaluation": "Provas e trabalhos",
  "bibliographyDescription": "DEITEL, Paul. Java: Como Programar",
  "students": [
    {
      "personId": "1001",
      "username": "joao.silva",
      "name": "João",
      "shortname": "Silva",
      "email": "joao.silva@example.com"
    }
  ],
  "teachers": [
    {
      "personId": "2001",
      "username": "prof.ana",
      "name": "Ana",
      "shortname": "Professora",
      "email": "ana.prof@example.com"
    }
  ]
}
```

## 🧪 Como Testar

### Opção 1: PowerShell Script

Execute o script de teste (certifique-se de estar no diretório `demo/`):

```powershell
cd demo
.\test-sync-rabbitmq.ps1
```

Ou execute diretamente:

```powershell
.\demo\test-sync-rabbitmq.ps1
```

### Opção 2: Management Console

1. Acesse http://localhost:15672
2. Login com admin/admin
3. Vá em **Queues** → `moodle.sync.course.queue`
4. Em **Publish message**, cole o JSON
5. Clique em **Publish message**

### Opção 3: Código Java

```java
@Autowired
private ProducerTemplate producerTemplate;

public void syncCourse(CourseSyncRequest request) {
    String json = objectMapper.writeValueAsString(request);
    producerTemplate.sendBody("direct:syncCourse", json);
}
```

## 📊 Resposta da Sincronização

A rota retorna um JSON com o resultado:

```json
{
  "groupId": "1234",
  "timestamp": "2026-02-02T10:30:00Z",
  "courseCreation": {
    "id": 42,
    "fullname": "Programação Java - Turma 2026"
  },
  "courseId": 42,
  "usersCreated": [
    {
      "username": "joao.silva",
      "type": "student",
      "response": {...}
    }
  ],
  "enrollments": [
    {
      "username": "joao.silva",
      "role": "student",
      "success": true
    },
    {
      "username": "prof.ana",
      "role": "teacher",
      "success": true
    }
  ],
  "enrollmentSuccess": 4,
  "enrollmentFailed": 0,
  "sectionUpdate": {...},
  "success": true,
  "message": "Course synchronized successfully"
}
```

## 🔧 Troubleshooting

### Erro: "Connection refused"
- Verifique se o RabbitMQ está em execução
- Confirme host e porta no application.properties

### Erro: "Queue not found"
- Crie a fila manualmente no RabbitMQ
- Verifique o nome da fila na configuração

### Erro: "Moodle authentication failed"
- Verifique o token do Moodle
- Confirme que o token tem permissões necessárias

### Usuários não são criados
- Verifique se já existem no Moodle (não haverá erro)
- Confirme que o webservice `core_user_create_users` está habilitado

## 📚 Serviços Moodle Necessários

Habilite os seguintes webservices no Moodle:

- `core_course_create_courses`
- `core_user_create_users`
- `enrol_manual_enrol_users`
- `core_course_update_courses`
- `core_course_search_courses`

## 🔐 Roles do Moodle

- **Role 3** = Teacher (Editingteacher)
- **Role 5** = Student

## 📝 Logs

Acompanhe os logs para debug:

```
[MoodleCourseRoute] Starting course synchronization
[MoodleCourseRoute] Synchronizing group: 1234
[MoodleCourseRoute] Creating course: Programação Java
[MoodleCourseRoute] Creating 3 students
[MoodleCourseRoute] Creating 1 teachers
[MoodleCourseRoute] Enrolling students
[MoodleCourseRoute] Enrolling teachers
[MoodleCourseRoute] Updating course sections
[MoodleCourseRoute] Synchronization completed
```

## 🚀 Integração com Sistema Acadêmico

Para integrar com o sistema acadêmico (baseado no código PHP fornecido):

1. Busque os dados do grupo/turma no banco de dados
2. Monte o objeto `CourseSyncRequest`
3. Converta para JSON
4. Envie para a fila `moodle.sync.course.queue`

Exemplo PHP:

```php
$data = [
    'groupId' => $groupId,
    'courseData' => [
        'fullname' => $courseName,
        'shortname' => $shortName,
        'categoryid' => $categoryId,
        'summary' => $summary
    ],
    'students' => $students,
    'teachers' => $teachers,
    // ...
];

// Enviar para RabbitMQ
$connection = new AMQPStreamConnection('localhost', 5672, 'admin', 'admin');
$channel = $connection->channel();
$channel->basic_publish(
    new AMQPMessage(json_encode($data)),
    'moodle_exchange',
    'moodle.sync.course.queue'
);
```
