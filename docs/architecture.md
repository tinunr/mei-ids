# Architecture and Runtime Flow

## Scope
MEI-IDS is an integration platform that connects an academic domain, Moodle, and notifications through RabbitMQ. The orchestrator module validates, transforms, and routes messages using Spring Boot + Apache Camel.

## Component Map
- Producers (academic system, test tools, future services) publish JSON messages to RabbitMQ.
- Orchestrator (Spring Boot + Camel) consumes messages, applies business rules, and calls the Moodle REST API.
- Moodle container hosts the LMS used by the integration flows.
- Notification service consumes RMQ events and sends emails via SMTP.

## Message Topology
- Main exchange: `moodle_exchange` (direct).
- Core queues:
  - `moodle.sync.course.queue` for course sync events.
  - `moodle.create.course.queue` is configured but not currently wired to a Camel route.
  - `moodle-create-course-queues` is the active queue used by the REST course creation flow.
- Demo exchange/queue:
  - Exchange `mei-ids-exchange-example`
  - Queue `mei-ids-queue-example`
  - Routing key `mei-ids-routing-key`
- Notification queue: `notification-queue`.

## Flow A: Course Creation (HTTP -> RabbitMQ -> Moodle)
1. Client calls `POST /api/moodle/create-course` in [demo/src/main/java/com/example/demo/controller/MoodleController.java](demo/src/main/java/com/example/demo/controller/MoodleController.java).
2. The request is serialized and published to `moodle-create-course-queues` by [demo/src/main/java/com/example/demo/producer/MoodleCourseProducer.java](demo/src/main/java/com/example/demo/producer/MoodleCourseProducer.java).
3. [demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java](demo/src/main/java/com/example/demo/consumer/MoodleCourseQueueConsumer.java) consumes the message and calls Moodle via [demo/src/main/java/com/example/demo/services/MoodleService.java](demo/src/main/java/com/example/demo/services/MoodleService.java).
4. Moodle REST returns the creation result which is logged by the consumer.

## Flow B: Course Sync (RabbitMQ -> Camel -> Moodle)
1. A producer publishes JSON to `moodle.sync.course.queue` (routing key equals the queue name) using `moodle_exchange`.
2. [demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java](demo/src/main/java/com/example/demo/consumer/RabbitMqConsumer.java) forwards the raw JSON to the Camel route `direct:syncCourse`.
3. The Camel pipeline in [demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java](demo/src/main/java/com/example/demo/routes/MoodleCourseRoute.java) performs:
   - Course creation
   - User creation
   - Enrollments (students role 5, teachers role 3)
   - Section update
4. The route returns a JSON summary for logging.

## Flow C: Demo Example (HTTP -> RabbitMQ -> Camel)
1. Client calls `POST /api/example/send-message` in [demo/src/main/java/com/example/demo/controller/ExampleController.java](demo/src/main/java/com/example/demo/controller/ExampleController.java).
2. [demo/src/main/java/com/example/demo/producer/ExampleProducer.java](demo/src/main/java/com/example/demo/producer/ExampleProducer.java) publishes to the demo exchange.
3. [demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java](demo/src/main/java/com/example/demo/consumer/ExampleConsumer.java) forwards to `direct:exampleRoute`.
4. [demo/src/main/java/com/example/demo/routes/ExampleRoute.java](demo/src/main/java/com/example/demo/routes/ExampleRoute.java) logs the message.

## Flow D: Notifications (RabbitMQ -> Email)
1. A producer emits the event pattern `notification.send` to the `notification-queue`.
2. [notification/src/notification.controller.ts](notification/src/notification.controller.ts) consumes the event.
3. [notification/src/mailer.service.ts](notification/src/mailer.service.ts) sends the email via SMTP.

## Data Contracts (Current)
- Course creation uses `CourseRequest` fields: `fullname`, `shortname`, `categoryid`, and optional `summary`.
- Course sync uses `CourseSyncRequest` with `groupId`, `courseData`, `students`, `teachers`, and optional content fields (objectives, content, methodology, evaluation, bibliographyDescription).

## Configuration Sources
- Orchestrator config: [demo/src/main/resources/application.properties](demo/src/main/resources/application.properties).
- Infrastructure config: [docker-compose.yml](docker-compose.yml).

## Observability
- RabbitMQ Management UI on http://localhost:15672 provides queue metrics and message visibility.
- The orchestrator logs all route processing and Moodle REST calls at INFO/DEBUG levels.

## Notes on Current Wiring
- The `moodle.create.course.queue` property is defined in [demo/src/main/resources/application.properties](demo/src/main/resources/application.properties), but there is no `direct:createCourse` route. The active creation flow uses `moodle-create-course-queues` via the REST endpoint.
- To unify behavior, either add a `direct:createCourse` route or align the queue names and listeners.
