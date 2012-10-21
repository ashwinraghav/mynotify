package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pool {
	ArrayList<ExecutorService> pools;
	ArrayList<WatchService> watchers;
	ArrayList<ConcurrentHashMap<WatchKey, Path>> keys;
	HashMap<String, WatchService> file_subscriptions;

	final int nPools = 20;

	Pool(int poolSize) throws IOException {
		file_subscriptions = new HashMap<String, WatchService>();
		initializeThreadPools(poolSize);
	}

	public void watch(String path, boolean recursive_watch) throws IOException {

		if (other_subscribers_subscribe_to(path)) {
			// some thread is already writing into an exchange for the file
			// being subscribed to
		} else {
			WatchService ws = registerFileToRandomPool(path);
			file_subscriptions.put(path, ws);
		}
	}

	private WatchService registerFileToRandomPool(String path)
			throws IOException {

		Random randomGenerator = new Random();
		int index = randomGenerator.nextInt(nPools);

		WatchService watcher = watchers.get(index);
		ConcurrentHashMap<WatchKey, Path> keySet = keys.get(index);

		Path dir = Paths.get(path);

		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);

		/**
		 * Check if the current registration is an update or a new registration.
		 * This is a reundant path. Will not be reached in the current scheme of
		 * things
		 */
		Path prev = keySet.get(key);

		if (prev == null) {
			System.out.format("register: %s\n", dir);
		} else {
			if (!dir.equals(prev)) {
				System.out.format("update: %s -> %s\n", prev, dir);
			}
		}
		/************************************/

		keySet.put(key, dir);
		return watcher;
	}

	private boolean other_subscribers_subscribe_to(String path) {
		return file_subscriptions.containsKey(path);
	}

	public void stopWatching(String path) throws IOException {
		Path dir = Paths.get(path);
		WatchService watcher = file_subscriptions.get(path);
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		ConcurrentHashMap<WatchKey, Path> keySet = keys.get(watchers
				.indexOf(watcher));

		if (true) {
			Path prev = keySet.get(key);
			if (prev == null) {
				System.out.format(
						"This directory is not under subscription: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					keySet.remove(key);
					System.out.format("removed subscription to: %s -> %s\n",
							prev, dir);
				}
			}
		}
		file_subscriptions.remove(path);
		key.cancel();
	}

	public void shutdown() {
		for (int i = 0; i < nPools; i++) {
			pools.get(i).shutdown();
		}
	}

	private void initializeThreadPools(int threadsPerPool) throws IOException {
		this.watchers = new ArrayList<WatchService>();
		this.pools = new ArrayList<ExecutorService>();
		this.keys = new  ArrayList<ConcurrentHashMap<WatchKey, Path>>();
		
		for (int i = 0; i < nPools; i++) {
			pools.add(Executors.newFixedThreadPool(threadsPerPool));
			watchers.add(FileSystems.getDefault().newWatchService());
			keys.add(new ConcurrentHashMap<WatchKey, Path>());

			for (int j = 0; j < threadsPerPool; j++) {
				try {
					SubscriberThread subscriberThread = new SubscriberThread(
							watchers.get(watchers.size() - 1), keys.get(keys
									.size() - 1));
					pools.get(pools.size() - 1).execute(subscriberThread);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

		}
	}

}