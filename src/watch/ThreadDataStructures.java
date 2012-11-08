package watch;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.concurrent.ConcurrentHashMap;

public class ThreadDataStructures {
	ConcurrentHashMap<WatchKey, Path> watchKeyToPath;

	public ThreadDataStructures() {
		this.watchKeyToPath = new ConcurrentHashMap<WatchKey, Path>();
	}

	public Path getPathFor(WatchKey key) {
		return watchKeyToPath.get(key);
	}

	public void removeKey(WatchKey key) {
		watchKeyToPath.remove(key);
		
	}

	public boolean areAllDirectoriesInaccessible() {
		return watchKeyToPath.isEmpty();
	}

	public void put(WatchKey key, Path dir) {
		watchKeyToPath.put(key, dir);
	}

}
