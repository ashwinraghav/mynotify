package watch;

import java.nio.file.*;
import java.nio.file.WatchEvent.Kind;
import static java.nio.file.StandardWatchEventKinds.*;
import java.io.*;
import java.util.*;

public class LocalWatcher {
	
		private final WatchService watcher;
		private final Map<WatchKey, Path> keys;

		@SuppressWarnings("unchecked")
		static <T> WatchEvent<T> cast(WatchEvent<?> event) {
			return (WatchEvent<T>) event;
		}
		
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
		public void register(Path dir) throws IOException {
			WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE,ENTRY_MODIFY);
			keys.put(key, dir);
		}

		/**
		 * Process all events for keys queued to the watcher
		 */
		void processEvents() {
			int overflows = 0;
			for (;;) {

				// wait for key to be signalled
				WatchKey key;
				try {
					key = watcher.take();
				} catch (InterruptedException x) {
					return;
				}

				Path dir = keys.get(key);
				if (dir == null) {
					System.err.println("WatchKey not recognized!!");
					continue;
				}

				for (WatchEvent<?> event : key.pollEvents()) {
					Kind<?> kind = event.kind();
					
					//System.out.println(event);

					// TBD - provide example of how OVERFLOW event is handled
					if (kind == OVERFLOW) {
						System.out.println("Overflowing!!!!!!!!!!!!!!!!!!!!!");
						overflows +=1;
						continue;
					}

					// Context for directory entry event is the file name of entry
					WatchEvent<Path> ev = cast(event);
					Path name = ev.context();
					
					Path child = dir.resolve(name);
					System.out.format("%s: %s\n", event.kind().name(), child);
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
}
