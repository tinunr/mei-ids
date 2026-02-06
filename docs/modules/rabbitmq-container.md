# RabbitMQ Container (rabbitmq)

## Purpose
Provides the message broker for all integration flows.

## Build Details
- Dockerfile: [rabbitmq/Dockerfile](rabbitmq/Dockerfile)
- Base image: `rabbitmq:3.13-management-alpine`

## Runtime Settings
The service is started via [docker-compose.yml](docker-compose.yml):
- AMQP port: 5672
- Management UI: http://localhost:15672
- Default credentials: `admin` / `admin`
