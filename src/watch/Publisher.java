package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

import com.google.gson.Gson;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

public class Publisher {
	ConnectionFactory factory;
	Connection connection;
	Channel channel;
	String exchangeType = "fanout";

	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		bootstrap();
	}

	public boolean publish(Path path, WatchEvent<?> event) throws IOException {
		// Exchange with the name of the file
		String exchangeName = path.resolve((Path) cast(event).context())
				.toString();
		// Convert the event to a serializable File Event that can be sent over
		// the network
		// This is a bit hacky. But works!
		SerializableFileEvent sfe = new SerializableFileEvent(event);
		String jsonized = new Gson().toJson(sfe);
		channel.basicPublish(exchangeName, "", null, jsonized.getBytes());

		System.out.println("Server says: I sent " + jsonized);
		// On the client, when you dequeue from rabit, simply do the following
		// Assuming jsonized is the json string that has been dequeued at the client
		
		/*
		 * SerializableFileEvent s = new Gson().fromJson(jsonized,
		 * SerializableFileEvent.class);
		 * 
		 * System.out.format("%s: detected on %s\n", s.kind().name(),
		 * s.context().toString());
		 */
		return true;
	}

	private void bootstrap() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost("elmer.cs.virginia.edu");
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
}
