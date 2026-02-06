# Notification Service (notification)

## Purpose
This module consumes RabbitMQ events and sends email notifications via SMTP.

## Runtime Entry
The microservice is started in [notification/src/main.ts](notification/src/main.ts) and listens to:
- RMQ URL: `amqp://admin:admin@localhost:5672`
- Queue: `notification-queue` (durable)

## Message Handling
- Event pattern: `notification.send`
- Handler: [notification/src/notification.controller.ts](notification/src/notification.controller.ts)
- Email delivery: [notification/src/mailer.service.ts](notification/src/mailer.service.ts)

## SMTP Configuration
The mailer uses environment variables (all optional where noted):
- `SMTP_HOST`, `SMTP_PORT`
- `SMTP_USER` or `SMTP_USERNAME`
- `SMTP_PASS` or `SMTP_PASSWORD`
- `SMTP_FROM` (optional; default is username or `no-reply@example.com`)
- `SMTP_ENCRYPTION` (values: `tls`, `ssl`, `none`)
- `SMTP_TLS_REJECT_UNAUTHORIZED` (default `true` when TLS is enabled)
- `SMTP_POOL`, `SMTP_DEBUG`
- `SMTP_CONNECTION_TIMEOUT`, `SMTP_SOCKET_TIMEOUT`

## Test Sender
A local test publisher exists in [notification/src/send-test-email.ts](notification/src/send-test-email.ts). It emits `notification.send` to `notification-queue`.

## Commands
See scripts in [notification/package.json](notification/package.json) for build and run tasks.
