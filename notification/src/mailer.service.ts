import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import nodemailer, { Transporter } from 'nodemailer';

export interface SendMailPayload {
  to: string;
  subject: string;
  html?: string;
  text?: string;
}

@Injectable()
export class MailerService {
  private readonly logger = new Logger(MailerService.name);
  private transporter: Transporter;

  constructor(private readonly config: ConfigService) {
    const host = this.config.get<string>('SMTP_HOST');
    const port = Number(this.config.get<string>('SMTP_PORT'));
    // Support both naming styles
    const user =
      this.config.get<string>('SMTP_USER') ||
      this.config.get<string>('SMTP_USERNAME');
    const pass =
      this.config.get<string>('SMTP_PASS') ||
      this.config.get<string>('SMTP_PASSWORD');

    // Encryption: 'tls' (STARTTLS), 'ssl' (465), or 'none'
    const enc = (
      this.config.get<string>('SMTP_ENCRYPTION') || ''
    ).toLowerCase();
    const secure = enc === 'ssl' || port === 465; // SSL uses port 465
    const requireTLS = enc === 'tls' && !secure; // STARTTLS on 587

    this.transporter = nodemailer.createTransport({
      host,
      port: port || undefined,
      secure,
      requireTLS,
      auth: user && pass ? { user, pass } : undefined,
    });
  }

  async sendMail(payload: SendMailPayload): Promise<void> {
    const from =
      this.config.get<string>('SMTP_FROM') ||
      this.config.get<string>('SMTP_USERNAME') ||
      'no-reply@example.com';
    const { to, subject, html, text } = payload;

    this.logger.log(`Sending email to ${to} with subject "${subject}"`);
    await this.transporter.sendMail({ from, to, subject, html, text });
    this.logger.log(`Email sent to ${to}`);
  }
}
