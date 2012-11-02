package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ClientSubscriber {

	public static void subscribe(String dirName,
			WatchEvent.Kind<?>... subscriptionTypes) {

		try {
			RPCManager rpcManager = new RPCManager();

			Object[] parameters = new Object[] { new String(dirName) };
			// Object[] parameters = new Object[]{new Integer(2), new
			// Integer(4), new Integer(6)};
			String i = (String) rpcManager.execute("HandlerClass.register",
					parameters);

			System.out.println("Returned: " + i);

			ExchangeManager exchangeManager = new ExchangeManager();
			Channel channel = exchangeManager.createChannel();
			exchangeManager.declareExchange(channel, dirName,
					Constants.exchangeMap);

			System.out.println("Client says name of exchange is: " + dirName);

			// bind all subscriptions
			String queueName = channel.queueDeclare().getQueue();
			for (Kind<?> w : subscriptionTypes) {
				channel.queueBind(queueName, dirName, w.name());
			}
			// *****

			System.out.println(" [*] Waiting for messages.");
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String jsonizedMessage = new String(delivery.getBody());

				System.out.println(" [x] Received: " + jsonizedMessage);
			}
			// exchangeManager.closeChannel(channel);
		} catch (Exception e) {
			System.out.println("RPC_Client: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] a) {
		subscribe("/localtmp/dump/0/1", ENTRY_CREATE, ENTRY_DELETE);
	}
	
}
