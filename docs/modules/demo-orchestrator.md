# Orchestrator Module (demo)

## Purpose
The demo module is the central orchestrator. It exposes REST endpoints, consumes RabbitMQ messages, and uses Apache Camel routes to coordinate calls to Moodle.

## Main Entry Points
REST controllers:
- Course creation: [demo/src/main/java/com/example/demo/controller/MoodleController.java](demo/src/main/java/com/example/demo/controller/MoodleController.java)
- RabbitMQ test endpoints: [demo/src/main/java/com/example/demo/controller/RabbitMqTestController.java](demo/src/main/java/com/example/demo/controller/RabbitMqTestController.java)
- Example test endpoint: [demo/src/main/java/com/example/demo/controller/ExampleController.java](demo/src/main/java/com/example/demo/controller/ExampleController.java)

RabbitMQ consumers:
- Course creation queue listener: [demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java](demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java)
- Sync course listener: [demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java](demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java)
- Example listener: [demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java](demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java)

Camel routes:
- Sync course flow: [demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java](demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java)
- Example route: [demo/src/main/java/com/example/demo/routes/ExampleRoute.java](demo/src/main/java/com/example/demo/routes/ExampleRoute.java)

## Messaging Behavior
Course creation (HTTP -> queue -> Moodle):
- Producer: [demo/src/main/java/com/example/demo/producer/MoodleCourseProducer.java](demo/src/main/java/com/example/demo/producer/MoodleCourseProducer.java)
- Queue: `moodle-create-course-queues`
- Consumer: [demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java](demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java)

Course sync (RabbitMQ -> Camel):
- Queue: `moodle.sync.course.queue` via `moodle_exchange`
- Listener: [demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java](demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java)
- Route: `direct:syncCourse` in [demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java](demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java)

Example flow (HTTP -> RabbitMQ -> Camel):
- Exchange/queue/binding: [demo/src/main/java/com/example/demo/config/RabbitMqExampleConfig.java](demo/src/main/java/com/example/demo/config/RabbitMqExampleConfig.java)
- Producer: [demo/src/main/java/com/example/demo/producer/ExampleProducer.java](demo/src/main/java/com/example/demo/producer/ExampleProducer.java)
- Consumer: [demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java](demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java)

## Key Services
- Moodle REST client: [demo/src/main/java/com/example/demo/services/MoodleService.java](demo/src/main/java/com/example/demo/services/MoodleService.java)
- RabbitMQ health/test producer: [demo/src/main/java/com/example/demo/producer/RabbitMqHealthCheckProducer.java](demo/src/main/java/com/example/demo/producer/RabbitMqHealthCheckProducer.java)

## Configuration
All settings are in [demo/src/main/resources/application.properties](demo/src/main/resources/application.properties):
- `server.port=7073`
- RabbitMQ host, exchange, and queue names
- Moodle REST URL and token
- `app.rabbitmq.enabled` must be set to `true` to enable the `RabbitMqConsumer`
- PostgreSQL connection settings (for future use)

## How to Run Locally
From the repository root:
1. Start infrastructure via [docs/modules/docker-compose.md](docs/modules/docker-compose.md)
2. Run the orchestrator:
   - `cd demo`
   - `./mvnw spring-boot:run`

## Related Guides
- Example flow: [docs/demo/EXEMPLO_COMPLETO_README.md](docs/demo/EXEMPLO_COMPLETO_README.md)
- Course sync flow: [docs/demo/SYNC_COURSE_README.md](docs/demo/SYNC_COURSE_README.md)
- Moodle service fix notes: [docs/demo/MOODLE_SERVICE_FIX.md](docs/demo/MOODLE_SERVICE_FIX.md)
