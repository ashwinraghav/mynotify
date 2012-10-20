package watch;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pool {
	ExecutorService pool;
	HashMap<String, SubscriberThread> file_subscriptions;
	ArrayList<SubscriberThread> subscriberThreads;

	Pool(int poolSize) {
		file_subscriptions = new HashMap<String, SubscriberThread>();
		pool = Executors.newFixedThreadPool(poolSize);

		initializeThreadPool(poolSize);
	}

	public void watch(String path, boolean recursive_watch) throws IOException {

		if (other_subscribers_subscribe_to(path)) {
			// some thread is already writing into an exchange for the file
			// being subscribed to
		} else {
			SubscriberThread thread = pickRandomThread();

			thread.register(new SubscriptionDetails(Paths.get(path),
					recursive_watch));
			file_subscriptions.put(path, thread);
		}

		// This needs to change. We end up having multiple Subscription
		// Objects even if threads cannot accommodate them

	}

	private boolean other_subscribers_subscribe_to(String path) {
		return file_subscriptions.containsKey(path);
	}

	private SubscriberThread pickRandomThread() {
		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(subscriberThreads.size());
		return subscriberThreads.get(index);
	}

	public void stopWatching(String path) throws IOException {
		file_subscriptions.get(path).unregister(
				new SubscriptionDetails(Paths.get(path), false));
	}

	public void shutdown() {
		pool.shutdown();
	}

	private void initializeThreadPool(int poolSize) {
		subscriberThreads = new ArrayList<SubscriberThread>();

		for (int i = 0; i < poolSize; i++) {
			SubscriberThread subscriberThread;
			try {
				subscriberThread = new SubscriberThread();
				subscriberThreads.add(subscriberThread);
				pool.execute(subscriberThread);
			} catch (IOException e) {
				e.printStackTrace();
			}

		}
	}
}