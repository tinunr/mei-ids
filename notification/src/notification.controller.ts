import { Controller, Logger } from '@nestjs/common';
import { EventPattern, Payload } from '@nestjs/microservices';
import type { SendMailPayload } from './mailer.service';
import { MailerService } from './mailer.service';

@Controller()
export class NotificationController {
  private readonly logger = new Logger(NotificationController.name);

  constructor(private readonly mailer: MailerService) {}

  // Consume RMQ messages with pattern 'notification.send'
  @EventPattern('notification.send')
  async handleSendEmail(@Payload() data: SendMailPayload): Promise<void> {
    this.logger.log(`Received message for email: ${JSON.stringify(data)}`);
    await this.mailer.sendMail(data);
  }
}
