#!/bin/bash
# Script de teste para criar cursos no Moodle via REST API

API_URL="http://localhost:7073/api/moodle/create-course"

echo "=== Teste de Criação de Curso via API REST ==="
echo ""
echo "URL da API: $API_URL"
echo "Timestamp: $(date)"
echo ""

# Teste 1: Criar um curso simples
echo "--- Teste 1: Criar um curso simples ---"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "courses": [
      {
        "fullname": "Curso de Teste 1",
        "shortname": "teste-001",
        "categoryid": 1,
        "idnumber": "TST-001",
        "summary": "Este é um curso de teste criado via API",
        "summaryformat": 1,
        "format": "topics",
        "visible": 1
      }
    ]
  }' \
  | jq .

echo ""
echo ""

# Teste 2: Criar múltiplos cursos
echo "--- Teste 2: Criar múltiplos cursos ---"
curl -X POST "$API_URL" \
  -H "Content-Type: application/json" \
  -d '{
    "courses": [
      {
        "fullname": "Curso de Teste 2",
        "shortname": "teste-002",
        "categoryid": 1,
        "idnumber": "TST-002",
        "summary": "Primeiro curso",
        "visible": 1
      },
      {
        "fullname": "Curso de Teste 3",
        "shortname": "teste-003",
        "categoryid": 1,
        "idnumber": "TST-003",
        "summary": "Segundo curso",
        "visible": 1
      }
    ]
  }' \
  | jq .

echo ""
echo "=== Testes concluídos ==="
echo ""
echo "Ver logs da aplicação para detalhes de processamento:"
echo "  - Logs da API: MoodleController"
echo "  - Logs do Producer: MoodleCourseProducer"
echo "  - Logs do Consumer: MoodleCourseQueueConsumer"
echo "  - Logs do Serviço: MoodleService"
