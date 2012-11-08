package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/* To Do
 * 1. Change in mem structures to database storage
 */
public class PoolManager {
	/*****
	 * pools -list of pools containing `poolSize` threads each watchers -list of
	 * inotify instances. Each pool is given 1 instance keys -each pool (every
	 * thread in it) needs a hashmap of keys that represent a file under
	 * subscription and the name of the file itself. A list of these hashmaps is
	 * needed. One for each pool file_subscriptions -map of file_name and the
	 * inotify instance that it is mapped to.
	 * 
	 * These data structures are redundant. But they store only references. So
	 * that is acceptable since it brings in convenience while programming.
	 */
	// ArrayList<ExecutorService> pools;
	ArrayList<Pool> pools;
	ConcurrentHashMap<String, Pool> file_subscriptions;

	CleanupManager cleanupManager;
	final int threadsPerPool = 20;
	int poolSize;

	/* bootstraps all the data structures */
	PoolManager(int poolSize) throws IOException {
		this.poolSize = poolSize;
		file_subscriptions = new ConcurrentHashMap<String, Pool>();
		initializeThreadPools();
		startCleanupManager();

	}

	private void startCleanupManager() throws IOException {
		this.cleanupManager = new CleanupManager(pools, file_subscriptions);
		Thread t = new Thread(cleanupManager);
		t.start();
	}

	/* watch a file */
	public void watch(String path, boolean recursive_watch) throws IOException {
		if (other_subscribers_subscribe_to(path)) {
			// some thread is already writing into an exchange for the file
			// being subscribed to
		} else {
			Pool tds = registerFileToRandomPool(path);
			file_subscriptions.put(path, tds);
		}
	}

	/*
	 * pick a random pool to handle the subscription and subscribe to the
	 * inotify instance that the pool is notified by.
	 */
	private Pool registerFileToRandomPool(String path) throws IOException {

		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(this.poolSize);

		Pool td = pools.get(index);
		Path dir = Paths.get(path);

		WatchKey key = dir.register(td.getWatchService(), ENTRY_CREATE,
				ENTRY_DELETE, ENTRY_MODIFY);
		td.getWatchKeyToPath().put(key, dir);

		return td;
	}

	private boolean other_subscribers_subscribe_to(String path) {
		return file_subscriptions.containsKey(path);
	}

	/* stop all threads in all pools */
	public void shutdown() {
		for (int i = 0; i < this.poolSize; i++) {
			pools.get(i).getThreadPool().shutdown();
		}
	}

	/*
	 * all threads in all pools are kept alive throughout the lifetime of the
	 * server. So no thread spawned on the fly. Those threads that belong to
	 * pools that do not yet handle a subscription are simply alive and waiting.
	 */
	private void initializeThreadPools() throws IOException {
		this.pools = new ArrayList<Pool>();
		for (int i = 0; i < this.poolSize; i++) {

			pools.add(new Pool(threadsPerPool));

			for (int j = 0; j < this.threadsPerPool; j++) {
				try {
					SubscriberThread subscriberThread = new SubscriberThread(
							pools.get(pools.size() - 1));
					pools.get(pools.size() - 1).getThreadPool().execute(
							subscriberThread);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}