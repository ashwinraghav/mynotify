package watch;

import java.io.IOException;
import java.util.ArrayList;
import org.apache.xmlrpc.XmlRpcException;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class DistributedWatcher {
	private final RPCManager rpcManager;
	private final ExchangeManager exchangeManager;
	private Channel channel;
	private String queueName;
	
	/**
	 * Creates a WatchService
	 */
	public DistributedWatcher() throws IOException {
		this.rpcManager = new RPCManager();
		this.exchangeManager = new ExchangeManager();
		this.channel = exchangeManager.createChannel();
		this.queueName = channel.queueDeclare().getQueue();
		System.out.println("Name of my queue: "+this.queueName);
	}

	/**
	 * Register the given directory with the WatchService
	 */
	public int register(String dirName) throws IOException {
			
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
			
			exchangeManager.declareExchange(channel, dirName,
					Constants.exchangeMap);
			
			channel.queueBind(queueName, dirName, "*");
			System.out.println("Bound exchange: "+dirName);
			return 0;
	}

	/**
	 * Process all events for keys queued to the watcher
	 */
	public ArrayList<SerializableFileEvent> pollEvents() {
		
		QueueingConsumer consumer = new QueueingConsumer(channel);
		
		try {
			this.channel.basicConsume(this.queueName,true,consumer);
		} catch (IOException e) {
			return null;
		}

		ArrayList<SerializableFileEvent> watchEvents = new ArrayList<SerializableFileEvent>();
		
		long end = System.currentTimeMillis() + 2;
		QueueingConsumer.Delivery delivery;
		
		while(System.currentTimeMillis() < end){
			try {
				//delivery = consumer.nextDelivery(1);
				delivery = consumer.nextDelivery();
			} catch (InterruptedException x) {
				continue;
			}
			if(delivery != null)
				watchEvents.add(SerializableFileEvent.constructFromJson(new String(delivery.getBody())));
		}
		
		return watchEvents;
	}
}
