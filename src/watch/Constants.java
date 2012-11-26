package watch;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.MessageProperties;
import com.rabbitmq.client.AMQP.Exchange.DeclareOk;

public class Constants {
	public static final Map<String, Object> exchangeMap;
	static {
		Map<String, Object> tempMap = new HashMap<String, Object>();
		tempMap.put("type", "fanout");
		tempMap.put("autoDelete", true);
		tempMap.put("durable", false);
		tempMap.put("internal", false);
		tempMap.put("arguments", null);
		exchangeMap = Collections.unmodifiableMap(tempMap);

	}
	public static final String host = "elmer.cs.virginia.edu";
	public static String serverUrl;
	public static long eventAccumulationWaitTime = 10;// milliseconds
	public static int serverPort;
	static {
		serverPort = 8080;
		serverUrl = "http://localhost:" + serverPort;
	}
	static ConnectionFactory factory = new ConnectionFactory();
	static Connection connection;
	public static Channel channel;
	static {
		factory.setHost(Constants.host);
		try {
			connection = factory.newConnection();
			channel = connection.createChannel();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Dispatcher dispatcher;
	static {
		try {
			dispatcher = new Dispatcher();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
