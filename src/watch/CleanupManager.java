package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import watch.Publisher.ExchangeManager;

public class CleanupManager implements Runnable {

	ArrayList<WatchService> watchers;
	ArrayList<ConcurrentHashMap<WatchKey, Path>> keys;
	ConcurrentHashMap<String, WatchService> file_subscriptions;

	ConnectionFactory factory;
	Connection connection;
	Channel channel;

	CleanupManager(ArrayList<WatchService> watchers,
			ArrayList<ConcurrentHashMap<WatchKey, Path>> keys,
			ConcurrentHashMap<String, WatchService> file_subscriptions)
			throws IOException {
		this.watchers = watchers;
		this.file_subscriptions = file_subscriptions;
		this.keys = keys;

		factory = new ConnectionFactory();
		factory.setHost(Constants.host);
		connection = factory.newConnection();
		channel = connection.createChannel();
	}

	private void cleanSubscriptions() {
		Iterator<?> it = file_subscriptions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String exchangeName = (String) entry.getKey();
			try {
				ExchangeManager.declareExchangePassive(channel, exchangeName);
			} catch (Exception e) {
				try {
					stopWatching(exchangeName);
					channel = connection.createChannel();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}

		}
	}

	public void stopWatching(String path) throws IOException {
		Path dir = Paths.get(path);

		WatchService watcher = file_subscriptions.get(path);

		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);

		ConcurrentHashMap<WatchKey, Path> keySet = keys.get(watchers
				.indexOf(watcher));

		Path prev = keySet.get(key);
		if (prev == null) {
			System.out.format("This directory is not under subscription: %s\n",
					dir);
		} else {
			if (dir.equals(prev)) {
				key.cancel();
				file_subscriptions.remove(path);
				keySet.remove(key);
				System.out.format("removed subscription to: %s -> %s\n", prev,
						dir);
			}
		}
	}

	@Override
	public void run() {
		while (true) {
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			cleanSubscriptions();
		}
	}
}
