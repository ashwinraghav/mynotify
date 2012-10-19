package watch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pool {
	ExecutorService pool;

	HashMap<String, Subscription> file_subscriptions;

	Pool(int poolSize) {
		file_subscriptions = new HashMap<String, Subscription>();
		pool = Executors.newFixedThreadPool(poolSize);
	}

	public void startWatching(String path, boolean recursive_watch)
			throws IOException {
		file_subscriptions.containsKey(path);
		Subscription subscription = new Subscription(Paths.get(path),
				recursive_watch);

		// This needs to change. We end up having multiple Subscription
		// Objects even if threads cannot accommodate them

		file_subscriptions.put(path, subscription);
		pool.execute(subscription);
	}

	public void stopWatching(String path) {
		file_subscriptions.get(path).stop();
	}

	public void close() {
		pool.shutdown();
	}
}