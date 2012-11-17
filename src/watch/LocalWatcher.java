package watch;

import java.nio.file.*;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.*;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class LocalWatcher {
	
		private WatchService watcher;
		private Map<WatchKey, Path> keys;
		
		/**
		 * Creates a WatchService
		 */
		public LocalWatcher() throws IOException {
			System.out.println("Initializing watcher");
			this.watcher = FileSystems.getDefault().newWatchService();
			this.keys = new HashMap<WatchKey, Path>();
		}

		/**
		 * Register the given directory with the WatchService
		 */
		public void register(String dirName) throws IOException {
			Path dir = Paths.get(dirName);
			WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,ENTRY_MODIFY);
			keys.put(key, dir);
		}

		/**
		 * Process all events for keys queued to the watcher
		 */
		public ArrayList<SerializableFileEvent> pollEvents() {
			
			ArrayList<SerializableFileEvent> watchEvents = new ArrayList<SerializableFileEvent>();
			WatchKey key;
			
			long end = System.currentTimeMillis() + 3;
			
			while(System.currentTimeMillis() < end){
				key=null;
				try {
					key = this.watcher.poll(1, TimeUnit.MILLISECONDS);
				} catch (InterruptedException x) {
					return null;
				}
				if(key == null){
					continue;
				}
				
				Path dir = keys.get(key);
				
				if (dir == null) {
					continue;
				}
				
				for (WatchEvent<?> event : key.pollEvents()){
					watchEvents.add(new SerializableFileEvent(event));
				}
				
				boolean valid = key.reset();
				if (!valid) {
					keys.remove(key);

					if (keys.isEmpty()) {
						break;
					}
				}
			}
			return watchEvents;
		}
}
