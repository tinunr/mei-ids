# Fluxo Completo de Exemplo - MEI-IDS

## 📋 Arquitetura

```
┌──────────────┐
│  REST API    │
│  /api/example│
│ /send-message│
└──────┬───────┘
       │ POST com mensagem
       ▼
┌──────────────────────────┐
│   ExampleProducer        │
│  (Envia para RabbitMQ)   │
└──────┬───────────────────┘
       │
       ▼
┌──────────────────────────────────────┐
│  RabbitMQ                            │
│  Exchange: mei-ids-exchange-example  │
│  Queue: mei-ids-queue-example        │
│  Routing Key: mei-ids-routing-key    │
└──────┬───────────────────────────────┘
       │
       ▼
┌──────────────────────────┐
│   ExampleConsumer        │
│   @RabbitListener        │
└──────┬───────────────────┘
       │ Encaminha para rota Camel
       ▼
┌──────────────────────────┐
│  ExampleRoute (Camel)    │
│  direct:exampleRoute     │
│  Mostra no LOG           │
└──────────────────────────┘
```

## 🧪 Como Testar

### 1. Inicie a Aplicação

```bash
cd d:\mei\mei-ids\demo
mvn clean spring-boot:run
```

Aguarde até ver:
```
[Application started]
[ExampleRoute] Route started
```

### 2. Envie uma Mensagem via REST API

**Opção A: cURL**

```bash
# Teste com mensagem padrão
curl -X POST http://localhost:7070/api/example/test

# Ou com mensagem customizada
curl -X POST "http://localhost:7070/api/example/send-message?message=Ola%20estou%20aqui%20-%20Customizado"
```

**Opção B: PowerShell**

```powershell
# Teste com mensagem padrão
Invoke-RestMethod -Uri "http://localhost:7070/api/example/test" -Method Post

# Com mensagem customizada
Invoke-RestMethod -Uri "http://localhost:7070/api/example/send-message?message=Ola%20estou%20aqui" -Method Post
```

**Opção C: Postman**

1. Criar uma nova requisição **POST**
2. URL: `http://localhost:7070/api/example/test`
3. Clique em **Send**

### 3. Verifique o Log

Você deve ver na console da aplicação:

```
[ExampleController] Recebido request para enviar mensagem: Ola estou aqui
[ExampleProducer] Enviando mensagem: Ola estou aqui
[ExampleProducer] Mensagem enviada com sucesso para o exchange 'mei-ids-exchange-example'
[ExampleConsumer] Mensagem recebida da fila 'mei-ids-queue-example': Ola estou aqui
[ExampleConsumer] Mensagem encaminhada para a rota 'direct:exampleRoute'
[ExampleRoute] ========================================
[ExampleRoute] PROCESSANDO MENSAGEM DO EXEMPLO
[ExampleRoute] Mensagem: Ola estou aqui
[ExampleRoute] Data/Hora: 2026-02-02 12:45:30
[ExampleRoute] ✓ Mensagem processada com sucesso: Ola estou aqui
[ExampleRoute] ========================================
```

## 📡 Exchange e Queue

A configuração cria **automaticamente**:

- **Exchange**: `mei-ids-exchange-example` (tipo Direct)
- **Queue**: `mei-ids-queue-example`
- **Routing Key**: `mei-ids-routing-key`

Verifique no RabbitMQ Management Console: `http://localhost:15672`

## 📊 Resposta da API

```json
{
  "status": "SUCCESS",
  "message": "Mensagem enviada para RabbitMQ",
  "payload": "Ola estou aqui",
  "queue": "mei-ids-queue-example",
  "exchange": "mei-ids-exchange-example",
  "timestamp": "2026-02-02T12:45:30"
}
```

## 🔧 Arquivos Criados

1. **RabbitMqExampleConfig.java** - Configuração do Exchange e Queue (criados automaticamente)
2. **ExampleProducer.java** - Envia mensagens para RabbitMQ
3. **ExampleConsumer.java** - Consome mensagens da fila
4. **ExampleRoute.java** - Rota Camel que processa as mensagens
5. **ExampleController.java** - REST API para enviar mensagens

## 💡 Fluxo Passo a Passo

1. ✅ REST API recebe request em `/api/example/send-message`
2. ✅ ExampleProducer envia mensagem para RabbitMQ
3. ✅ Exchange roteia para a Queue
4. ✅ ExampleConsumer recebe da Queue
5. ✅ Encaminha para rota Camel (`direct:exampleRoute`)
6. ✅ ExampleRoute processa e mostra no LOG

## 📝 Personalizar a Mensagem

Envie qualquer mensagem customizada:

```bash
curl -X POST "http://localhost:7070/api/example/send-message?message=Teste%20123"
```

## 🚀 Próximos Passos

Agora pode:
- Adaptar a rota para fazer processamento mais complexo
- Adicionar mais consumidores
- Integrar com outras rotas Camel
- Enviar para múltiplas filas
