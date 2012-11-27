package Dispatch;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import watch.Constants;

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

