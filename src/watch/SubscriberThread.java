package watch;

import static java.nio.file.LinkOption.NOFOLLOW_LINKS;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SubscriberThread implements Runnable {
	ArrayList<SubscriptionDetails> subscriptionDetails;
	private final WatchService watcher;
	private final Map<WatchKey, Path> keys;
	private boolean trace = false;
	volatile boolean being_watched;

	@SuppressWarnings("unchecked")
	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public void run() {
		processEvents();
	}

	SubscriberThread() throws IOException {
		this.being_watched = true;
		subscriptionDetails = new ArrayList<SubscriptionDetails>();
		// these can be a little dangerous here.
		// means that there are watchers even for threads that are not
		// scheduled.
		this.watcher = FileSystems.getDefault().newWatchService();
		this.keys = new HashMap<WatchKey, Path>();
	}

	public void stop() {
		being_watched = false;
	}

	void processEvents() {

		// enable trace after initial registration
		this.trace = true;

		while (being_watched) {

			// wait for key to be signalled
			WatchKey key;
			try {
				// this needs to be a poll if a client unsubscribes
				// before the subscription is scheduled itself.
				key = watcher.poll(5, TimeUnit.SECONDS);
			} catch (InterruptedException x) {
				return;
			}

			if (key == null)
				continue;

			Path dir = keys.get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}
			for (WatchEvent<?> event : key.pollEvents()) {

				WatchEvent.Kind kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// Context for directory entry event is the file name of entry
				WatchEvent<Path> ev = cast(event);
				Path name = ev.context();
				Path child = dir.resolve(name);

				// print out event
				System.out.format("%s: %s detected by %d\n", event.kind().name(), child, Thread.currentThread().getId());

				// if directory is created, and watching recursively, then
				// register it and its sub-directories
				
				/*if (recursive && (kind == ENTRY_CREATE)) {
					try {
						if (Files.isDirectory(child, NOFOLLOW_LINKS)) {
							registerAll(child);
						}
					} catch (IOException x) {
						// ignore to keep sample readbale
					}
				}*/
			}

			// reset key and remove from set if directory no longer accessible
			boolean valid = key.reset();
			if (!valid) {
				keys.remove(key);

				// all directories are inaccessible
				if (keys.isEmpty()) {
					break;
				}
			}
		}
	}

	/* regular file registrations */

	public void register(SubscriptionDetails s) throws IOException {
		this.subscriptionDetails.add(s);
		Path dir = s.getDirectory();
		WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY);
		if (trace) {
			Path prev = keys.get(key);
			if (prev == null) {
				System.out.format("register: %s\n", dir);
			} else {
				if (!dir.equals(prev)) {
					System.out.format("update: %s -> %s\n", prev, dir);
				}
			}
		}
		keys.put(key, dir);
	}

	/**
	 * Register the given directory, and all its sub-directories, with the
	 * WatchService.
	 */
	/*
	 * private void registerAll(final Path start) throws IOException { //
	 * register directory and sub-directories Files.walkFileTree(start, new
	 * SimpleFileVisitor<Path>() {
	 * 
	 * @Override public FileVisitResult preVisitDirectory(Path dir,
	 * BasicFileAttributes attrs) throws IOException { register(dir); return
	 * FileVisitResult.CONTINUE; } }); }
	 */

}
