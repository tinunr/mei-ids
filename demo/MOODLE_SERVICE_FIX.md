# MoodleService - Correção do Serviço de Criação de Cursos

## Problema Identificado
O serviço estava enviando a requisição em **formato JSON**, mas a API do Moodle espera dados em **application/x-www-form-urlencoded** com parâmetros no formato `courses[0][fullname]`, `courses[0][shortname]`, etc.

## Solução Implementada

### 1. **MoodleService.java** - Reescrito método `createCourse()`
- ✅ Alterado para enviar dados em **application/x-www-form-urlencoded**
- ✅ Parâmetros agora env formato `courses[0][fieldname]` conforme API Moodle espera
- ✅ **Logs detalhados** em TODOS os passos:
  - Início da operação
  - Parâmetros sendo enviados
  - URL da requisição
  - Status da resposta HTTP
  - Erros ou sucesso do Moodle
  - Stack trace completo em caso de exceção

### 2. **Importações Adicionadas**
```java
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
```

### 3. **Logging Aprimorado**
Adicionado em `MoodleService`:
- `createCourse()` - Logs completos de debug
- `enrollUser()` - Logs do processo de inscrição
- `createUser()` - Logs da criação de usuários

Log pattern em `application.properties`:
```properties
logging.level.com.example.demo=DEBUG
logging.pattern.console=%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
```

### 4. **Método Helper Adicionado**
```java
private Map<String, Object> createSuccessResponse(String message)
```

## Formato da Requisição da API

### Endpoint
```
POST /api/moodle/create-course
Content-Type: application/json
```

### Body (JSON)
```json
{
  "courses": [
    {
      "fullname": "Nome do Curso",
      "shortname": "curso-001",
      "categoryid": 1,
      "idnumber": "CURSO-001",
      "summary": "Descrição do curso",
      "summaryformat": 1,
      "format": "topics",
      "visible": 1
    }
  ]
}
```

## Fluxo de Processamento

```
1. POST /api/moodle/create-course
   ↓
2. MoodleController.createCourse() 
   [Log: "Recebido request para criar curso"]
   ↓
3. MoodleCourseProducer.sendCreateCourseRequest()
   [Log: "Solicitação de criação de curso enviada"]
   ↓
4. RabbitMQ (fila: moodle-create-course-queues)
   ↓
5. MoodleCourseQueueConsumer.processCourseCreationRequest()
   [Log: "Recebida mensagem da fila"]
   ↓
6. MoodleService.createCourse()
   [Log: Parâmetros, URL, Status HTTP, Erro/Sucesso Moodle]
   ↓
7. Resposta com resultado (sucesso/erro)
```

## Como Testar

### Usando PowerShell (Windows)
```powershell
.\test-moodle-course-api.ps1
```

### Usando Bash (Linux/Mac)
```bash
bash test-moodle-course-api.sh
```

### Usando cURL Manualmente
```bash
curl -X POST "http://localhost:7073/api/moodle/create-course" \
  -H "Content-Type: application/json" \
  -d '{
    "courses": [{
      "fullname": "Teste",
      "shortname": "teste",
      "categoryid": 1
    }]
  }'
```

## Verificando os Logs

A aplicação agora exibe logs detalhados em DEBUG. Procure por:

1. **[MoodleController]** - Recepção da requisição
2. **[MoodleCourseProducer]** - Envio para fila
3. **[MoodleCourseQueueConsumer]** - Recebimento da fila
4. **[MoodleService.createCourse]** - Processamento e criação:
   - `Iniciando criação de curso`
   - `Parâmetros da requisição`
   - `URL do Moodle`
   - `Resposta recebida`
   - `Erro do Moodle` (se houver)
   - `Curso criado com sucesso` (se houver)

## Exemplo de Log Esperado (Sucesso)

```
2026-02-06 16:50:00.123 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] Iniciando criação de curso: Teste
2026-02-06 16:50:00.124 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] Parâmetros da requisição: wstoken=***, wsfunction=core_course_create_courses, fullname=Teste, shortname=teste, categoryid=1
2026-02-06 16:50:00.125 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] URL do Moodle: http://localhost/webservice/rest/server.php
2026-02-06 16:50:00.126 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] Enviando POST request para: http://localhost/webservice/rest/server.php
2026-02-06 16:50:01.234 [main] INFO  com.example.demo.services.MoodleService - [MoodleService.createCourse] Resposta recebida - Status Code: 200
2026-02-06 16:50:01.235 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] Corpo da resposta: {id=123, shortname=teste, fullname=Teste}
2026-02-06 16:50:01.236 [main] INFO  com.example.demo.services.MoodleService - [MoodleService.createCourse] Curso criado com sucesso! Resposta: {id=123, shortname=teste, fullname=Teste}
```

## Exemplo de Log Esperado (Erro Moodle)

```
2026-02-06 16:50:00.123 [main] INFO  com.example.demo.services.MoodleService - [MoodleService.createCourse] Resposta recebida - Status Code: 200
2026-02-06 16:50:00.124 [main] DEBUG com.example.demo.services.MoodleService - [MoodleService.createCourse] Corpo da resposta: {exception=moodle_exception, errorcode=shortnamealreadyexists, message=Short name already exists}
2026-02-06 16:50:00.125 [main] ERROR com.example.demo.services.MoodleService - [MoodleService.createCourse] ERRO do Moodle: moodle_exception - Short name already exists
```

## Arquivos Modificados

1. **MoodleService.java**
   - Reescrito `createCourse()` para usar form-urlencoded
   - Logs adicionados a todos os métodos
   - Método `createSuccessResponse()` adicionado

2. **application.properties**
   - Logging levels configurados
   - Log pattern definido

3. **test-moodle-course-api.sh** (novo)
   - Script bash para testes

4. **test-moodle-course-api.ps1** (novo)
   - Script PowerShell para testes Windows

## Próximos Passos para Debugging

Se ainda não funcionar:

1. **Verifique o token**: O token em `app.moodle.token` está correto?
2. **URL do Moodle**: A URL em `app.moodle.url` está acessível?
3. **RabbitMQ**: A fila `moodle-create-course-queues` foi criada?
4. **Banco de Dados**: PostgreSQL está rodando?
5. **Logs**: Procure por `[MoodleService]` nos logs para ver erros específicos

## Compilação e Execução

```bash
# Compilar
mvn clean compile

# Executar
mvn spring-boot:run

# Ou direto com java após build
mvn clean package
java -jar target/demo-0.0.1-SNAPSHOT.jar
```

---

**Status**: ✅ Compilado com sucesso
**Próximo Teste**: Execute os scripts de teste e verifique os logs
