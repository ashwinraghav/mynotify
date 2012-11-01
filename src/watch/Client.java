package watch;
import java.net.URL;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

import watch.Publisher.ExchangeManager;

import java.io.IOException;
import com.rabbitmq.client.*;

public class Client {
		
	public static void main(String[] args) {
		
		//Server URL and port
		
		
		//Name of the directory to be watched and name of exchange
		String dirName = "/localtmp/dump/0/1";
		
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		
		try{
			config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(Constants.serverUrl));
			
			client = new XmlRpcClient();
			client.setConfig(config);
			
			//Parameters to be pushed to RPC Server handler
			Object[] parameters = new Object[]{new String(dirName)};
			//Object[] parameters = new Object[]{new Integer(2), new Integer(4), new Integer(6)};
			
			String i = (String)client.execute("HandlerClass.register", parameters);
			System.out.println("Returned: "+i);
			
			ConnectionFactory factory = new ConnectionFactory();
			factory.setHost(Constants.host);
			Connection connection = factory.newConnection();
			Channel channel = connection.createChannel();
			ExchangeManager.declareExchange(channel, dirName, Constants.exchangeMap);
			//channel.exchangeDeclare(dirName, "fanout");
			
			System.out.println("Client says name of exchange is: "+dirName);
			String queueName = channel.queueDeclare().getQueue();
			
			channel.queueBind(queueName, dirName, "*");
			
			System.out.println(" [*] Waiting for messages.");
			
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);
			
			while(true){
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String jsonizedMessage = new String(delivery.getBody());
				
				System.out.println(" [x] Received: "+jsonizedMessage);
			}
			
			
		}catch(Exception e){
			System.out.println("RPC_Client: "+e);
			e.printStackTrace();
		}
	}

}
