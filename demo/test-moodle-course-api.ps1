# Script de teste para criar cursos no Moodle via REST API (PowerShell)

$apiUrl = "http://localhost:7073/api/moodle/create-course"

Write-Host "=== Teste de Criacao de Curso via API REST ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "URL da API: $apiUrl" -ForegroundColor Yellow
Write-Host "Timestamp: $(Get-Date)" -ForegroundColor Yellow
Write-Host ""

# Teste 1: Criar um curso simples
Write-Host "--- Teste 1: Criar um curso simples ---" -ForegroundColor Green
$body1 = @{
    courses = @(
        @{
            fullname = "Curso de Teste 1"
            shortname = "teste-001"
            categoryid = 1
            idnumber = "TST-001"
            summary = "Este e um curso de teste criado via API"
            summaryformat = 1
            format = "topics"
            visible = 1
        }
    )
} | ConvertTo-Json

Write-Host "Enviando requisicao..." -ForegroundColor Yellow
Write-Host $body1 | ConvertTo-Json
Write-Host ""

$response1 = Invoke-WebRequest -Uri $apiUrl -Method Post -Headers @{"Content-Type" = "application/json"} -Body $body1
Write-Host "Resposta:" -ForegroundColor Green
Write-Host ($response1.Content | ConvertFrom-Json | ConvertTo-Json) -ForegroundColor White

Write-Host ""
Write-Host ""

# Teste 2: Criar múltiplos cursos
Write-Host "--- Teste 2: Criar múltiplos cursos ---" -ForegroundColor Green
$body2 = @{
    courses = @(
        @{
            fullname = "Curso de Teste 2"
            shortname = "teste-002"
            categoryid = 1
            idnumber = "TST-002"
            summary = "Primeiro curso"
            visible = 1
        },
        @{
            fullname = "Curso de Teste 3"
            shortname = "teste-003"
            categoryid = 1
            idnumber = "TST-003"
            summary = "Segundo curso"
            visible = 1
        }
    )
} | ConvertTo-Json

Write-Host "Enviando requisicao..." -ForegroundColor Yellow
Write-Host $body2 | ConvertTo-Json
Write-Host ""

$response2 = Invoke-WebRequest -Uri $apiUrl -Method Post -Headers @{"Content-Type" = "application/json"} -Body $body2
Write-Host "Resposta:" -ForegroundColor Green
Write-Host ($response2.Content | ConvertFrom-Json | ConvertTo-Json) -ForegroundColor White

Write-Host ""
Write-Host "=== Testes Concluidos ===" -ForegroundColor Cyan
Write-Host ""
Write-Host "Ver logs da aplicacao para detalhes de processamento:" -ForegroundColor Yellow
Write-Host "  - Logs da API: MoodleController" -ForegroundColor White
Write-Host "  - Logs do Producer: MoodleCourseProducer" -ForegroundColor White
Write-Host "  - Logs do Consumer: MoodleCourseQueueConsumer" -ForegroundColor White
Write-Host "  - Logs do Servico: MoodleService" -ForegroundColor White
