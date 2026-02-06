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

    const enableLogger =
      (this.config.get<string>('SMTP_DEBUG') || 'false')
        .toLowerCase()
        .trim() === 'true';
    const pool =
      (this.config.get<string>('SMTP_POOL') || 'false').toLowerCase().trim() ===
      'true';

    // Prefer IPv4 to avoid ENETUNREACH on IPv6-only blocked networks
    // Use net connect option `family: 4` instead of custom dns injection.

    this.transporter = nodemailer.createTransport({
      host,
      port: port || undefined,
      secure,
      requireTLS,
      auth: user && pass ? { user, pass } : undefined,
      pool,
      connectionTimeout:
        Number(this.config.get<string>('SMTP_CONNECTION_TIMEOUT')) || 10000,
      socketTimeout:
        Number(this.config.get<string>('SMTP_SOCKET_TIMEOUT')) || 15000,
      logger: enableLogger,
      debug: enableLogger,
      tls:
        enc === 'tls'
          ? {
              rejectUnauthorized:
                (
                  this.config.get<string>('SMTP_TLS_REJECT_UNAUTHORIZED') ||
                  'true'
                )
                  .toLowerCase()
                  .trim() === 'true',
            }
          : undefined,
      // Force IPv4 connections
      family: 4,
    } as any);
  }

  async sendMail(payload: SendMailPayload): Promise<void> {
    const from =
      this.config.get<string>('SMTP_FROM') ||
      this.config.get<string>('SMTP_USERNAME') ||
      'no-reply@example.com';
    const { to, subject, html, text } = payload;

    this.logger.log(`Sending email to ${to} with subject "${subject}"`);
    try {
      await this.transporter.sendMail({ from, to, subject, html, text });
      this.logger.log(`Email sent to ${to}`);
    } catch (err: any) {
      const msgUpper = String(err?.message || '').toUpperCase();
      const isConnTimeout =
        (err?.code === 'ESOCKET' && err?.command === 'CONN') ||
        msgUpper.includes('ETIMEDOUT');
      const isNetUnreach = msgUpper.includes('ENETUNREACH');

      this.logger.error(
        `Send failed: ${err?.message || err}. code=${err?.code} command=${err?.command}`,
      );

      // Autoretry via SSL 465 if 587/STARTTLS times out
      const host = this.config.get<string>('SMTP_HOST');
      const port = Number(this.config.get<string>('SMTP_PORT'));
      const enc = (
        this.config.get<string>('SMTP_ENCRYPTION') || ''
      ).toLowerCase();
      const user =
        this.config.get<string>('SMTP_USER') ||
        this.config.get<string>('SMTP_USERNAME');
      const pass =
        this.config.get<string>('SMTP_PASS') ||
        this.config.get<string>('SMTP_PASSWORD');

      const shouldFallback =
        (isConnTimeout || isNetUnreach) &&
        ((enc === 'tls' && port === 587) || port === 587);
      if (shouldFallback) {
        this.logger.warn(
          'Connection to 587 timed out. Retrying via SSL on port 465...',
        );

        const fallbackTransport = nodemailer.createTransport({
          host,
          port: 465,
          secure: true,
          requireTLS: false,
          auth: user && pass ? { user, pass } : undefined,
          connectionTimeout:
            Number(this.config.get<string>('SMTP_CONNECTION_TIMEOUT')) || 10000,
          socketTimeout:
            Number(this.config.get<string>('SMTP_SOCKET_TIMEOUT')) || 15000,
          family: 4,
        } as any);

        try {
          await fallbackTransport.sendMail({ from, to, subject, html, text });
          this.logger.log(`Email sent to ${to} via SSL fallback`);
          return;
        } catch (fallbackErr: any) {
          this.logger.error(
            `Fallback send failed: ${fallbackErr?.message || fallbackErr}. code=${fallbackErr?.code} command=${fallbackErr?.command}`,
          );
        }
      }

      throw err;
    }
  }
}
