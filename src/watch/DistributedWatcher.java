package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.xmlrpc.XmlRpcException;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class DistributedWatcher {
	private final RPCManager rpcManager;
	private final ExchangeManager exchangeManager;
	private ArrayList<String> paths;
	private final WatchEvent.Kind<?> subscriptionTypes[] = {ENTRY_CREATE, ENTRY_DELETE,
			ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP};
	private int pathIndex;
	private final Map<String, Channel> keys;
	
	/**
	 * Creates a WatchService
	 */
	public DistributedWatcher() throws IOException {
		this.rpcManager = new RPCManager();
		this.exchangeManager = new ExchangeManager();
		this.paths = new ArrayList<String>();
		this.keys = new HashMap<String, Channel>();
		this.pathIndex = 0;
	}

	/**
	 * Register the given directory with the WatchService
	 */
	public int register(String dirName) throws IOException {
			
			boolean autoDelete = true;
			boolean durable = false;
			
			paths.add(dirName);
			keys.put(dirName, this.exchangeManager.createChannel());
			
			Object[] parameters = new Object[] {dirName};
			String i;
			try {
				i = (String) rpcManager.execute("HandlerClass.register",
						parameters);
			} catch (XmlRpcException e) {
				System.err.println("Unhandled XML Exception Type");
				return -1;
			}
			
			if(i.startsWith("0: ")){
				System.err.println("Registration Failed");
				return -1;
			}
			else if(i.startsWith("1: ")){
				System.out.println("Registration Successful");
				System.out.println(i);
			}

			exchangeManager.declareExchange(keys.get(dirName), dirName,
					Constants.exchangeMap);
			
			
			// bind all subscriptions
			if (durable) {
				autoDelete = false;
			}
			
			String queueName = keys.get(dirName).queueDeclare("", durable,
					false, autoDelete, null).getQueue();
			
			for (Kind<?> w : subscriptionTypes) {
				keys.get(dirName).queueBind(queueName, dirName, w.name());
			}
			return 0;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public List<SerializableFileEvent> pollEvents() {
		
		boolean autoDelete = true;
		boolean durable = false;
		
		if(pathIndex == -1){
			System.err.println("Cannot poll; no existing registrations!");
			return null;
		}
		
		QueueingConsumer consumer = new QueueingConsumer(keys.get(paths.get(pathIndex)));
		String queueName;
		try {
			queueName = keys.get(paths.get(pathIndex)).queueDeclare("", durable, false, autoDelete, null).getQueue();
		} catch (IOException e) {
			System.err.println("Could not grab queuename");
			return null;
		}
		
		
		try {
			keys.get(pathIndex).basicConsume(queueName, true, consumer);
		} catch (IOException e) {
			System.err.println("Could not consume from the queue");
			return null;
		}

		List<SerializableFileEvent> watchEvents = new ArrayList<SerializableFileEvent>();
		long end = System.currentTimeMillis() + 2;
		QueueingConsumer.Delivery delivery;
		
		while(System.currentTimeMillis() < end){
			try {
				delivery = consumer.nextDelivery(1);
			} catch (InterruptedException x) {
				continue;
			}
			watchEvents.add(SerializableFileEvent.constructFromJson(new String(delivery.getBody())));
		}
		pathIndex++;
		return watchEvents;
	}
}
