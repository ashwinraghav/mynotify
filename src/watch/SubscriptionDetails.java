package watch;

import java.nio.file.Path;

public class SubscriptionDetails {
	private final boolean recursive;
	Path directory;

	public SubscriptionDetails(Path dir, boolean recursive) {
		this.directory = dir;
		this.recursive = recursive;
	}

	public Path getDirectory() {
		return directory;
	}
	
	public boolean isRecursive() {
		return recursive;
	}
	
}
