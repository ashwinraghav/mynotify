package demos;

import watch.*;

import watch.Constants;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ReceiveLogs {

	private static final String EXCHANGE_NAME = "/localtmp/dump/0/1";

	public static void main(String[] argv) throws java.io.IOException,
			java.lang.InterruptedException {

		ExchangeManager exchangeManager = new ExchangeManager();

		Channel channel = exchangeManager.createChannel();
		exchangeManager.declareExchange(channel, EXCHANGE_NAME,
				Constants.exchangeMap);

		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, EXCHANGE_NAME, "*");

		System.out.println(" [*] Waiting for messages. To exit press CTRL+C");

		QueueingConsumer consumer = new QueueingConsumer(channel);
		channel.basicConsume(queueName, true, consumer);

		while (true) {
			QueueingConsumer.Delivery delivery = consumer.nextDelivery();
			String message = new String(delivery.getBody());

			System.out.println(" [x] Received '" + message + "'");
		}
		//exchangeManager.closeChannel(channel);
	}
}