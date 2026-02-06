# Docker Compose Environment

## Services
Defined in [docker-compose.yml](docker-compose.yml):
- `rabbitmq` (broker + management UI)
- `moodleapp` (Moodle PHP/Apache)
- `moodledb` (MySQL 8.4.5)
- `phpmyadmin` (DB admin UI)

## Networks and Volumes
- Network: `mei-ids-network`
- Volumes: `rabbitmq_data`, `clouddbdata`, `moodledata`

## How to Run
From the repository root:
1. `docker compose up -d`
2. `docker compose ps`
3. `docker compose logs -f moodleapp`

## Default Ports
- RabbitMQ: 5672
- RabbitMQ UI: 15672
- Moodle: 80
- phpMyAdmin: 8081
