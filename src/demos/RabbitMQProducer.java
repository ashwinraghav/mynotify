package demos;
import watch.Constants;

import com.rabbitmq.client.*;


public class RabbitMQProducer {
	public static void main(String a[]) throws Exception {
		ConnectionFactory factory = new ConnectionFactory();
		factory.setUsername("guest");
		factory.setPassword("guest");
		factory.setVirtualHost("/");
		factory.setHost(Constants.host);
		factory.setPort(5672);
		Connection conn = factory.newConnection();
		Channel channel = conn.createChannel();
		String exchangeName = "myExchange";
		String routingKey = "testRoute";
		byte[] messageBodyBytes = "Hello, world!".getBytes();
		channel.basicPublish(exchangeName, routingKey,
				MessageProperties.PERSISTENT_TEXT_PLAIN, messageBodyBytes);
		channel.close();
		conn.close();
	}
}
