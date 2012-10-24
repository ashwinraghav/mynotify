package watch;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
/* To Do
 * 1. Push notifications to exchange instead of printing
 * 2. Collate notifications in case of redundancy
 */
public class SubscriberThread implements Runnable {
	/*watcher		-inotify instance
	 *keys			-a hashmap of keys that represent a file under subscription
	 * 				and the name of the file itself
	 */
	private final WatchService watcher;
	private final ConcurrentHashMap<WatchKey, Path> keys;
	volatile boolean being_watched;
	private Publisher publisher;

	public void run() {
		processEvents();
	}

	SubscriberThread(WatchService watcher, ConcurrentHashMap<WatchKey, Path> keys) throws IOException {
		this.being_watched = true;
		this.watcher = watcher;
		this.keys = keys;
		this.publisher = new Publisher();
	}

	public void stop() {
		being_watched = false;
	}

	void processEvents() {

		// enable trace after initial registration

		while (being_watched) {
			// wait for key to be signaled
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

				Kind<?> kind = event.kind();

				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}

				// print out event
				try {
					publisher.publish(dir, event);
				} catch (IOException e) {
					System.out.println("unable to publish for some reason");
					e.printStackTrace();
				}
				
				//placeholder 1
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
	
	//placeholder 1
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
