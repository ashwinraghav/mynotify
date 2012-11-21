package watch;

import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/* To Do
 * 1. Push notifications to exchange instead of printing
 * 2. Collate notifications in case of redundancy
 */
public class SubscriberThread implements Runnable {
	/*
	 * watcher -inotify instancekeys -a hashmap of keys that represent a file
	 * under subscription and the name of the file itself
	 */
	private Pool myPool;
	volatile boolean being_watched;
	private Publisher publisher;

	public void run() {
		processEvents();
	}

	private WatchService watcher() {
		return myPool.getWatchService();
	}

	private ConcurrentHashMap<WatchKey, Path> keys() {
		return myPool.getWatchKeyToPath();
	}

	private BurstController burstController() {
		return myPool.getBurstController();
	}

	SubscriberThread(Pool myPool) throws IOException {
		this.being_watched = true;
		this.myPool = myPool;
		this.publisher = new Publisher();
	}

	public void stop() {
		being_watched = false;
	}

	void processEvents() {
		while (being_watched) {
			WatchKey key;
			try {
				//key = watcher().poll(5, TimeUnit.SECONDS);
				key = watcher().take();
			} catch (InterruptedException x) {
				NotificationServer.log("ERROR: Could not access Watch Service");
				return;
			}

			if (key == null)
				continue;

			Path dir = keys().get(key);
			if (dir == null) {
				System.err.println("WatchKey not recognized!!");
				continue;
			}
			
			waitForEventsToAccumulate();
			ArrayList<SerializableFileEvent> serializableFileEvents = new ArrayList<SerializableFileEvent>();		
			for (WatchEvent<?> event : key.pollEvents()) {

				Kind<?> kind = event.kind();
				serializableFileEvents.add(new SerializableFileEvent(event, dir));
				// TBD - provide example of how OVERFLOW event is handled
				if (kind == OVERFLOW) {
					continue;
				}
				
				
			/*	try {
					boolean burstResult = burstController().checkBurst(key);
					//if (burstResult) {
						//publisher.publish(dir, new NotificationStopEvent(event));
					//} else{
						publisher.publish(dir, event);
					//}
				} catch (IOException e) {
					System.out.println("unable to publish for some reason");
					NotificationServer.log("Unable to Publish!");
					e.printStackTrace();
				}*/
				// placeholder 1
			}
			//System.out.println("Exiting that loop");
			try {
				publisher.publish(serializableFileEvents, dir);
			} catch (IOException e) {
				NotificationServer.log("Unable to Publish!");
				e.printStackTrace();
			}
			resetAndRemoveKeyIfInaccessible(key);
			if (areAllDirectoriesInaccessible()) {
				break;
			}
		}
	}

	private void waitForEventsToAccumulate() {
		try {
			Thread.sleep(Constants.eventAccumulationWaitTime);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
	}

	private boolean areAllDirectoriesInaccessible() {
		return keys().isEmpty();
	}

	private void resetAndRemoveKeyIfInaccessible(WatchKey key) {
		boolean valid = key.reset();
		if (!valid) {
			keys().remove(key);
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

	// placeholder 1
	// if directory is created, and watching recursively, then
	// register it and its sub-directories

	/*
	 * if (recursive && (kind == ENTRY_CREATE)) { try { if
	 * (Files.isDirectory(child, NOFOLLOW_LINKS)) { registerAll(child); } }
	 * catch (IOException x) { // ignore to keep sample readbale } }
	 */
}
