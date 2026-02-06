import { ClientProxyFactory, Transport } from '@nestjs/microservices';

async function main() {
  const client = ClientProxyFactory.create({
    transport: Transport.RMQ,
    options: {
      urls: ['amqp://admin:admin@localhost:5672'],
      queue: 'notification-queue',
      queueOptions: { durable: true },
    },
  });

  await client
    .emit('notification.send', {
      to: process.env.TEST_TO || 'you@example.com',
      subject: 'Teste de envio',
      html: '<p>Mensagem de teste via RMQ + SMTP</p>',
      text: 'Mensagem de teste via RMQ + SMTP',
    })
    .toPromise();

  process.exit(0);
}

main().catch((err) => {
  console.error(err);
  process.exit(1);
});
