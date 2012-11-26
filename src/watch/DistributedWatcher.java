package watch;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import org.apache.xmlrpc.XmlRpcException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConsumerCancelledException;
import com.rabbitmq.client.QueueingConsumer;
import com.rabbitmq.client.ShutdownSignalException;

public class DistributedWatcher {
	
	private RPCManager rpcManager;
	private ExchangeManager exchangeManager;
	private Channel channel;
	private String queueName;
	private QueueingConsumer consumer;

	/**
	 * Creates a watch service
	 * @param durable
	 * @throws IOException
	 */
	public DistributedWatcher(boolean durable) throws IOException {
		this.rpcManager = new RPCManager();
		this.exchangeManager = new ExchangeManager();
		this.channel = exchangeManager.createChannel();
		boolean autoDelete = !durable;
		this.queueName = channel.queueDeclare("", durable, false, autoDelete,null).getQueue();
		this.consumer = new QueueingConsumer(channel);
		this.channel.basicConsume(queueName, true, consumer);
	}

	/**
	 * Register the given directory with the WatchService
	 * @param dirName
	 * @param subscriptionTypes
	 * @return
	 * @throws IOException
	 */
	public int register(String dirName, WatchEvent.Kind<?>... subscriptionTypes)
			throws IOException {
		Object[] parameters = new Object[] { new String(dirName) };
		String i;
		try {
			i = (String) rpcManager.execute("HandlerClass.register", parameters);
		} catch (XmlRpcException e) {
			System.err.println("Unhandled XML Exception Type");
			return -1;
		}

		if (i.startsWith("0: ")) {
			System.err.println("Registration Failed");
			return -1;
		} else if (i.startsWith("1: ")) {
			System.out.println("Registration Successful");
			System.out.println(i);
		}

		exchangeManager.declareExchange(channel, dirName, Constants.exchangeMap);

		for (Kind<?> w : subscriptionTypes) {
			channel.queueBind(this.queueName, dirName, w.name());
		}

		System.out.println("Bound exchange: " + dirName);
		return 0;
	}

	/**
	 * Process all events for keys queued to the watcher
	 * @throws InterruptedException
	 * @throws ConsumerCancelledException
	 * @throws ShutdownSignalException
	 */
	public ArrayList<SerializableFileEvent> pollEvents() throws ShutdownSignalException, ConsumerCancelledException, InterruptedException {

		ArrayList<SerializableFileEvent> watchEvents = new ArrayList<SerializableFileEvent>();
		QueueingConsumer.Delivery delivery;
		delivery = consumer.nextDelivery(1);
		
		if (delivery != null) {
			watchEvents.addAll(SerializableFileEvent
					.constructFromJsonArray(new String(delivery.getBody())));
		}

		return watchEvents;
	}
}
