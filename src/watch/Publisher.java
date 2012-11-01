package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Map;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class Publisher {
	ConnectionFactory factory;
	Connection connection;
	Channel channel;
	String exchangeType = "fanout";

	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		
	}

	public boolean publish(Path path, WatchEvent<?> event) throws IOException {
		bootstrap();
		// Exchange with the name of the file (but this only works for a file,
		// in the case of a directory.. this gets hairy)
		// String exchangeName = path.resolve((Path) cast(event).context())
		// .toString();
		String exchangeName = path.resolve(path).toString();
		channel = connection.createChannel();
		// channel.exchangeDeclare(exchangeName, "fanout");
		ExchangeManager.declareExchange(channel, exchangeName, Constants.exchangeMap);
		
		
		// Convert the event to a serializable File Event that can be sent over
		// the network
		// This is a bit hacky. But works!
		SerializableFileEvent sfe = new SerializableFileEvent(event);
		String jsonized = new Gson().toJson(sfe);
		channel.basicPublish(exchangeName, "", null, jsonized.getBytes());

		System.out.println("Server says: I sent " + jsonized);
		// On the client, when you dequeue from rabit, simply do the following
		// Assuming jsonized is the json string that has been dequeued at the
		// client

		/*
		 * SerializableFileEvent s = new Gson().fromJson(jsonized,
		 * SerializableFileEvent.class);
		 * 
		 * System.out.format("%s: detected on %s\n", s.kind().name(),
		 * s.context().toString());
		 */
		channel.close();
		connection.close();
		return true;
	}

	private void bootstrap() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost("elmer.cs.virginia.edu");
		connection = factory.newConnection();
	}

	static class ExchangeManager{
		@SuppressWarnings("unchecked")
		public static DeclareOk declareExchange(Channel channel, String exchangeName, Map<String, Object> m) throws IOException{
			System.out.println("Server says name of Exchange is: " + exchangeName);
			return channel.exchangeDeclare(exchangeName, (String)m.get("type"),
					(Boolean)m.get("durable"),(Boolean) m.get("autoDelete"),
					(Boolean)m.get("internal"),(Map<String, Object>) m.get("Arguments"));
			
		}
	}
}
