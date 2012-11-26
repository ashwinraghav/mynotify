package watch;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;

public class Dispatcher {
	private final Queue<Dispatchable> messageQueue;
	static ExecutorService threadPool;
	static int dispatcherPoolSize = 5;
	static {
		threadPool = Executors.newCachedThreadPool();
	}

	public void lazyPublish(String exchangeName, byte[] body) {
		Dispatchable dispatchable = new Dispatchable(exchangeName, body);
		messageQueue.offer(dispatchable);

		synchronized (messageQueue) {
			messageQueue.notifyAll();
		}

	}

	public Dispatcher() throws IOException {
		this.messageQueue = new ConcurrentLinkedQueue<Dispatchable>();

		for (int i = 0; i < this.dispatcherPoolSize; i++) {
			Thread t = new Thread(new Consumer(messageQueue));
			threadPool.execute(t);
		}

	}
}

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

class Dispatchable {
	String exchangeName;
	byte[] body;

	public Dispatchable(String exchangeName, byte[] body) {
		this.exchangeName = exchangeName;
		this.body = body;
	}
}
