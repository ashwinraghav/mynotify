package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class CleanupManager implements Runnable {

	ArrayList<Pool> keys;
	ConcurrentHashMap<String, Pool> file_subscriptions;

	ExchangeManager exchangeManager;

	CleanupManager(ArrayList<Pool> tStructs,
			ConcurrentHashMap<String, Pool> file_subscriptions)
			throws IOException {
		this.file_subscriptions = file_subscriptions;
		this.keys = tStructs;

		exchangeManager = new ExchangeManager();
	}

	@SuppressWarnings("unchecked")
	private void cleanSubscriptions() {
		Iterator<?> it = file_subscriptions.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry entry = (Map.Entry) it.next();
			String exchangeName = (String) entry.getKey();
			if (!exchangeManager.isDeclared(exchangeName)) {
				try {
					stopWatching(exchangeName);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void stopWatching(String path) throws IOException {
		Path dir = Paths.get(path);

		Pool tds = file_subscriptions.get(path);

		WatchKey key = dir.register(tds.getWatchService(), ENTRY_CREATE,
				ENTRY_DELETE, ENTRY_MODIFY);

		Path prev = tds.getWatchKeyToPath().get(key);
		if (prev == null) {
			System.out.format("This directory is not under subscription: %s\n",
					dir);
		} else {
			if (dir.equals(prev)) {
				key.cancel();
				file_subscriptions.remove(path);
				tds.getWatchKeyToPath().remove(key);
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
