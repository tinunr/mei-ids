# Script PowerShell para testar a sincronização de curso via RabbitMQ
# Instale o RabbitMQ.Client antes: dotnet add package RabbitMQ.Client

$rabbitHost = "localhost"
$rabbitPort = 5672
$rabbitUser = "admin"
$rabbitPass = "admin"
$queueName = "moodle.sync.course.queue"

# Payload de exemplo para sincronização
$payload = @"
{
  "groupId": "1234",
  "courseData": {
    "fullname": "Programação Java - Turma 2026",
    "shortname": "JAVA2026",
    "categoryid": 1,
    "summary": "Curso de Programação Java para iniciantes"
  },
  "objectives": "Aprender os conceitos fundamentais de programação orientada a objetos usando Java",
  "content": "1. Introdução ao Java\n2. Variáveis e Tipos de Dados\n3. Estruturas de Controle\n4. POO - Classes e Objetos\n5. Herança e Polimorfismo",
  "methodology": "Aulas expositivas, práticas em laboratório, projetos individuais e em grupo",
  "evaluation": "Provas escritas (40%), Trabalhos práticos (40%), Participação (20%)",
  "bibliographyDescription": "DEITEL, Paul. Java: Como Programar. 10ª ed.\nSIERRA, Kathy. Use a Cabeça! Java.",
  "students": [
    {
      "personId": "1001",
      "username": "joao.silva",
      "name": "João",
      "shortname": "Silva",
      "email": "joao.silva@example.com"
    },
    {
      "personId": "1002",
      "username": "maria.santos",
      "name": "Maria",
      "shortname": "Santos",
      "email": "maria.santos@example.com"
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
"@

Write-Host "Enviando mensagem para RabbitMQ..." -ForegroundColor Cyan

# Usando curl para enviar via RabbitMQ Management API
$base64Auth = [Convert]::ToBase64String([Text.Encoding]::ASCII.GetBytes("${rabbitUser}:${rabbitPass}"))
$headers = @{
    "Authorization" = "Basic $base64Auth"
    "Content-Type" = "application/json"
}

$body = @{
    properties = @{}
    routing_key = $queueName
    payload = $payload
    payload_encoding = "string"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "http://${rabbitHost}:15672/api/exchanges/%2F/moodle_exchange/publish" `
                                   -Method Post `
                                   -Headers $headers `
                                   -Body $body

    Write-Host "✓ Mensagem enviada com sucesso!" -ForegroundColor Green
    Write-Host "Response: $($response | ConvertTo-Json)" -ForegroundColor Yellow
} catch {
    Write-Host "✗ Erro ao enviar mensagem: $_" -ForegroundColor Red
    Write-Host "Certifique-se de que:" -ForegroundColor Yellow
    Write-Host "  1. RabbitMQ está em execução" -ForegroundColor Yellow
    Write-Host "  2. Management Plugin está habilitado: rabbitmq-plugins enable rabbitmq_management" -ForegroundColor Yellow
    Write-Host "  3. As credenciais estão corretas" -ForegroundColor Yellow
}
