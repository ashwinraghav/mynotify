package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Arrays;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

public class ClientSubscriber {

	public static void nfssubscribe(String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes) {

		try {
			boolean autoDelete = true;
			RPCManager rpcManager = new RPCManager(Constants.host);

			Object[] parameters = new Object[] { new String(dirName) };
			String i = (String) rpcManager.execute("HandlerClass.register",
					parameters);

			System.out.println("Returned: " + i);

			ExchangeManager exchangeManager = new ExchangeManager();
			Channel channel = exchangeManager.createChannel();
			exchangeManager.declareExchange(channel, dirName,
					Constants.exchangeMap);

			System.out.println("Client says name of exchange is: " + dirName);

			// bind all subscriptions
			if (durable) {
				autoDelete = false;
			}
			String queueName = channel.queueDeclare("", durable, false,
					autoDelete, null).getQueue();
			for (Kind<?> w : subscriptionTypes) {
				channel.queueBind(queueName, dirName, w.name());
			}
			// *****

			System.out.println(" [*] Waiting for messages.");
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String jsonizedMessage = new String(delivery.getBody());
				System.out.println(" [x] Received: " + jsonizedMessage);
			}
			// exchangeManager.closeChannel(channel);
		} catch (Exception e) {
			System.out.println("RPC_Client: " + e);
			e.printStackTrace();
		}
	}

	public static void main(String[] a) {
		boolean durable = false;
		boolean isNFS = false;
		String directory = "/if8/am2qa/temp/1";
		ArrayList<String> mountedDirectories = new ArrayList<String>();

		// Allow user to pass name of directory as command line argument
		if (a.length == 1)
			directory = a[0];

		// Check if directory is local or mounted via NFS
		// Unfortunately, ever OS is different, so we're going to need to parse
		// `mount`s differently
		// The main difference between mac and linux is the placement of the
		// mount type
		String os = System.getProperty("os.name").toLowerCase();
		String searchString = null;

		if (os.contains("mac")) {
			System.out.println("You're running on a mac!");
			searchString = "(nfs";
		} else if ((os.contains("nix") || (os.contains("linux")))) {
			System.out.println("You're running on Linux/Unix Machine!");
			searchString = "type nfs";
		}

		String line = "";

		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("mount");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			ArrayList<String> mount_info = new ArrayList<String>();

			while ((line = b.readLine()) != null) {
				mount_info.add(line);
			}

			for (int i = 0; i < mount_info.size(); i++) {
				if (mount_info.get(i).contains(searchString)) {
					ArrayList<String> splitLine = new ArrayList<String>(Arrays
							.asList(mount_info.get(i).split(" ")));
					int indexOfPath = splitLine.indexOf("on");
					String mountPoint = splitLine.get(++indexOfPath);
					System.out.println("Found a mounted NFS directory!: "
							+ mountPoint);
					mountedDirectories.add(mountPoint);
				}
			}

		} catch (Exception e) {
			System.out.println("Error running `mount` command");
			e.printStackTrace();
			System.exit(-1);
		}

		for (int i = 0; i < mountedDirectories.size(); i++) {
			if (directory.startsWith(mountedDirectories.get(i))) {
				isNFS = true;
				break;
			}
		}

		if (isNFS)
			System.out.println("This directory is Remotely Accessed!");
		else
			System.out.println("This directory is locally accessible");

		nfssubscribe(directory, durable, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);

	}

}
