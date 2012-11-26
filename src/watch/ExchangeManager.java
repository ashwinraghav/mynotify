package watch;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class ExchangeManager {

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

	public boolean sendPassively(String exchangeName, byte[] body) {
		Constants.dispatcher.lazyPublish(exchangeName, body);
		return true;
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
		return Constants.channel;
	}

	public void closeChannel(Channel channel) {
		/*
		 * try { if (channel.isOpen()) { channel.close(); } } catch (IOException
		 * e) { e.printStackTrace(); }
		 */

	}

	public String declareQueueAndBindToExchange(Channel channel,
			String exchangeName, String routingKey) throws IOException {
		String queueName = channel.queueDeclare().getQueue();
		channel.queueBind(queueName, exchangeName, routingKey);
		return queueName;
	}

}
