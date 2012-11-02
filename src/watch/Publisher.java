package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class Publisher {
	ConnectionFactory factory;
	Connection connection;
	Channel channel;
	
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost(Constants.host);
		connection = factory.newConnection();
	}

	public boolean publish(Path path, WatchEvent<?> event) throws IOException {
		channel = connection.createChannel();
		// String exchangeName = path.resolve((Path) cast(event).context())
		// .toString();
		String exchangeName = path.resolve(path).toString();

		try {
			System.out.println("Server says name of Exchange is: "
					+ exchangeName);
			ExchangeManager.declareExchangePassive(channel, exchangeName);
			String jsonized = (new SerializableFileEvent(event)).toJson();
			channel.basicPublish(exchangeName, "", null, jsonized.getBytes());
			System.out.println("Server says: I sent " + jsonized);
			channel.close();
		} catch (IOException e) {
			System.out.println(e);
		}
		return true;
	}


	public static class ExchangeManager {
		@SuppressWarnings("unchecked")
		public static DeclareOk declareExchange(Channel channel,
				String exchangeName, Map<String, Object> m) throws IOException {
			System.out.println("Name of Exchange is: "
					+ exchangeName);
			return channel.exchangeDeclare(exchangeName,
					(String) m.get("type"), (Boolean) m.get("durable"),
					(Boolean) m.get("autoDelete"), (Boolean) m.get("internal"),
					(Map<String, Object>) m.get("Arguments"));

		}

		public static DeclareOk declareExchangePassive(Channel channel,
				String exchangeName) throws IOException {
			return channel.exchangeDeclarePassive(exchangeName);
		}
	}
}
