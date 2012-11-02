package watch;

import java.io.IOException;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class ExchangeManager {

	ConnectionFactory factory;
	Connection connection;
	Channel channel;

	public ExchangeManager() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost(Constants.host);
		connection = factory.newConnection();
	}

	public DeclareOk declareExchange(String exchangeName, Map<String, Object> m)
			throws IOException {
		Channel channel = createChannel();
		DeclareOk declareOk = declareExchange(channel, exchangeName, m);
		closeChannel(channel);
		return declareOk;
	}

	@SuppressWarnings("unchecked")
	public DeclareOk declareExchange(Channel channel, String exchangeName,
			Map<String, Object> m) throws IOException {
		return channel.exchangeDeclare(exchangeName, (String) m.get("type"),
				(Boolean) m.get("durable"), (Boolean) m.get("autoDelete"),
				(Boolean) m.get("internal"), (Map<String, Object>) m
						.get("Arguments"));
	}

	public boolean sendPassively(String exchangeName, String routingKey,
			byte[] body) {
		Channel channel = createChannel();
		boolean success = false;
		try {
			if (declarePassiveExchange(channel, exchangeName)) {
				channel.basicPublish(exchangeName, routingKey, null, body);
				success = true;
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		closeChannel(channel);
		return success;
	}

	public static boolean declarePassiveExchange(Channel channel,
			String exchangeName) {
		try {
			channel.exchangeDeclarePassive(exchangeName);
			return true;
		} catch (IOException e) {
			// e.printStackTrace();
			return false;
		}
	}

	public boolean isDeclared(String exchangeName) {
		Channel channel = createChannel();
		boolean declared = declarePassiveExchange(channel, exchangeName);
		closeChannel(channel);
		return declared;
	}

	public Channel createChannel() {
		try {
			return connection.createChannel();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public void closeChannel(Channel channel) {
		try {
			if (channel.isOpen())
				channel.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String declareQueueAndBindToExchange(Channel channel,
			String exchangeName, String routingKey) throws IOException {
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, exchangeName, routingKey);
		return queueName;
	}

}