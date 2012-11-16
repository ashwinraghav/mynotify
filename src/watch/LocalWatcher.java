package watch;

import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import static java.nio.file.StandardWatchEventKinds.*;

import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LocalWatcher {
	
		private final WatchService watcher;
		private final Map<WatchKey, Path> keys;
		private List<SerializableFileEvent> watchEvents;
		
		/**
		 * Creates a WatchService
		 */
		public LocalWatcher() throws IOException {
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey, Path>();
		}

		/**
		 * Register the given directory with the WatchService
		 */
		public void register(String dirName) throws IOException {
			Path dir = Paths.get(dirName);
			WatchKey key = dir.register(this.watcher, ENTRY_CREATE, ENTRY_DELETE,ENTRY_MODIFY);
			keys.put(key, dir);
			System.out.println("Size of key map: "+keys.size());
		}

		/**
		 * Process all events for keys queued to the watcher
		 */
		public List<SerializableFileEvent> pollEvents() {
			
			watchEvents = new ArrayList<SerializableFileEvent>();
			WatchKey key;
			
			long end = System.currentTimeMillis() + 2;
			
			System.out.println("About to poll");
			while(System.currentTimeMillis() < end){
				
				try {
					key = this.watcher.poll(1,  TimeUnit.MILLISECONDS);
				} catch (InterruptedException x) {
					System.err.println("Returning Null!");
					return null;
				}
				if(key == null){
					return null;
				}
				

				Path dir = keys.get(key);
				if (dir == null) {
					System.err.println("WatchKey not recognized!!");
					continue;
				}
				
				for (WatchEvent<?> event : key.pollEvents()){
					watchEvents.add(new SerializableFileEvent(event));
				}
			}
			System.out.println("Size of watch events: "+watchEvents.size());
			return watchEvents;
		}
}
