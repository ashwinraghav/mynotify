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
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class CleanupManager implements Runnable {

	ArrayList<WatchService> watchers;
	ArrayList<ConcurrentHashMap<WatchKey, Path>> keys;
	ConcurrentHashMap<String, WatchService> file_subscriptions;

	ExchangeManager exchangeManager;

	CleanupManager(ArrayList<WatchService> watchers,
			ArrayList<ConcurrentHashMap<WatchKey, Path>> keys,
			ConcurrentHashMap<String, WatchService> file_subscriptions)
			throws IOException {
		this.watchers = watchers;
		this.file_subscriptions = file_subscriptions;
		this.keys = keys;

		exchangeManager = new ExchangeManager();
	}

	@SuppressWarnings("unchecked")
	private void cleanSubscriptions() {
		Iterator<?> it = file_subscriptions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String exchangeName = (String) entry.getKey();
			if (! exchangeManager.isDeclared(exchangeName)) {
				try {
					stopWatching(exchangeName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void stopWatching(String path) throws IOException{
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
