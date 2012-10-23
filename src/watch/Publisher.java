package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.Date;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import demos.Constants;

public class Publisher {
	ConnectionFactory factory;
	Connection connection;
	Channel channel;
	boolean inProduction = true;
	String exchangeType = "fanout";
	
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		bootstrap();
	}

	public boolean publish(Path path, WatchEvent event) throws IOException {

		WatchEvent<Path> ev = cast(event);
		Path name = ev.context();
		Path child = path.resolve(name);

		String exchangeName = child.toString();

		System.out.format("%s: %s detected by %d at", event.kind().name(),
				child, Thread.currentThread().getId());
		System.out.println(new Date().getTime());

		
		String message = "dummy_message";

		if (inProduction) {
			channel.exchangeDeclare(exchangeName, exchangeType);
			channel.basicPublish(exchangeName, "", null, message.getBytes());
		}
		
		System.out.println(" [x] Sent '" + message + "'");
		return true;
	}

	private void bootstrap() throws IOException {
		factory = new ConnectionFactory();
		factory.setHost("elmer.cs.virginia.edu");
		connection = factory.newConnection();
		channel = connection.createChannel();
	}
}
