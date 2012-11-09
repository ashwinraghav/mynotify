package watch;

import java.nio.file.Path;
import java.nio.file.WatchEvent;

public final class NotificationStopEvent extends SerializableFileEvent {

	private final static int time = 10 * 1000; // milliseconds
	public static final WatchEvent.Kind<Path> NOTIFICATION_STOP = new StdWatchEventKind<Path>(
			"NOTIFICATION_STOP", Path.class);

	public NotificationStopEvent(WatchEvent<?> event) {
		super(NOTIFICATION_STOP.name(), event.context().toString(), time);
	}
}
