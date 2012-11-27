package watch;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Pool {
	private ConcurrentHashMap<WatchKey, Path> watchKeyToPath;
	private WatchService watchService;
	ExecutorService threadPool;
	private BurstController burstController;

	public ExecutorService getThreadPool() {
		return threadPool;
	}

	public void setThreadPool(ExecutorService pool) {
		this.threadPool = pool;
	}

	public Pool(int threadsPerPool) throws IOException {
		this.watchKeyToPath = new ConcurrentHashMap<WatchKey, Path>();
		watchService = FileSystems.getDefault().newWatchService();
		this.threadPool = Executors.newFixedThreadPool(threadsPerPool);
		this.burstController = new BurstController();
	}

	public BurstController getBurstController() {
		return burstController;
	}

	public ConcurrentHashMap<WatchKey, Path> getWatchKeyToPath() {
		return watchKeyToPath;
	}

	public void setWatchKeyToPath(
			ConcurrentHashMap<WatchKey, Path> watchKeyToPath) {
		this.watchKeyToPath = watchKeyToPath;
	}

	public WatchService getWatchService() {
		return watchService;
	}

	public void setWatchService(WatchService watchService) {
		this.watchService = watchService;
	}

	public void qput(WatchKey key, Path dir) {
		watchKeyToPath.put(key, dir);
	}

}
