package Dispatch;

import java.io.IOException;
import java.util.Queue;

import watch.Constants;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

class Consumer implements Runnable {
	private final Queue<Dispatchable> queue;
	private Channel channel;

	public Consumer(Queue<Dispatchable> queue) throws IOException {
		this.queue = queue;
		ConnectionFactory factory = new ConnectionFactory();
		factory.setHost(Constants.host);
		Connection connection = factory.newConnection();
		channel = connection.createChannel();
	}

	@Override
	public void run() {
		while (true) {

			consume();

			try {
				synchronized (queue) {
					queue.wait();
				}
			} catch (InterruptedException ex) {

			}
		}
	}

	private void consume() {

		while (!queue.isEmpty()) {

			Dispatchable dispatchable = queue.poll();
			if (dispatchable != null) {
				String routingKey = "";
				try {

					// removing the exchange decleration improves performance
					// significantly
					channel.basicPublish(dispatchable.exchangeName,
							routingKey, MessageProperties.PERSISTENT_BASIC,
							dispatchable.body);

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}