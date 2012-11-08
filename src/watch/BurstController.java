package watch;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BurstController {
	private HashMap<Path, Long> keys;

	public BurstController() {
		keys = new HashMap<Path, Long>();
	}

	public boolean isBurst(Path dir) {
		if (keys.get(dir) == null) {
			keys.put(dir, new Date().getTime());
			return true;
		}
		
		return false;
	}

}
