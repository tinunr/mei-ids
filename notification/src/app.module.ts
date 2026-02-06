import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { ScheduleModule } from '@nestjs/schedule';
import { AppController } from './app.controller';
import { AppService } from './app.service';
import { MailerService } from './mailer.service';
import { NotificationController } from './notification.controller';

@Module({
  imports: [ScheduleModule.forRoot(), ConfigModule.forRoot({ isGlobal: true })],
  controllers: [AppController, NotificationController],
  providers: [AppService, MailerService],
})
export class AppModule {}
