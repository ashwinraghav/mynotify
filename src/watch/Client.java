package watch;

import java.net.MalformedURLException;
import java.net.URL;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import watch.Publisher.ExchangeManager;

import java.io.IOException;
import com.rabbitmq.client.*;

public class Client {

	public static void main(String[] args) {

		String dirName = "/localtmp/dump/0/1";

		try {
			XmlRpcClient client = getRPCClient();
			Object[] parameters = new Object[] { new String(dirName) };
			// Object[] parameters = new Object[]{new Integer(2), new
			// Integer(4), new Integer(6)};

			String i = (String) client.execute("HandlerClass.register",
					parameters);
			System.out.println("Returned: " + i);

			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Constants.host);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			ExchangeManager.declareExchange(channel, dirName,
					Constants.exchangeMap);
			// channel.exchangeDeclare(dirName, "fanout");

			System.out.println("Client says name of exchange is: " + dirName);
			String queueName = channel.queueDeclare().getQueue();

			channel.queueBind(queueName, dirName, "*");

			System.out.println(" [*] Waiting for messages.");

			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String jsonizedMessage = new String(delivery.getBody());

				System.out.println(" [x] Received: " + jsonizedMessage);
			}

		} catch (Exception e) {
			System.out.println("RPC_Client: " + e);
			e.printStackTrace();
		}
	}

	private static XmlRpcClient getRPCClient() throws MalformedURLException {
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(Constants.serverUrl));

		client = new XmlRpcClient();
		client.setConfig(config);
		return client;
	}

}
