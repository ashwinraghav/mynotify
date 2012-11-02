package watch;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import com.rabbitmq.client.*;

public class ClientSubscriber {

	public static void subscribe(String dirName) {

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
			String queueName = exchangeManager.declareQueueAndBindToExchange(
					channel, dirName, "*");

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
		subscribe("/localtmp/dump/0/1");
	}

}
