package watch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;

import com.google.gson.Gson;

public class SerializableFileEvent implements WatchEvent {
	String eventName;
	String context;
	int count;

	public SerializableFileEvent(WatchEvent<?> event) {
		eventName = event.kind().name();
		context = event.context().toString();
		count = event.count();
	}

	public static SerializableFileEvent constructFromJson(String jsonized) {
		return new Gson().fromJson(jsonized, SerializableFileEvent.class);
	}

	@Override
	public Object context() {
		return Paths.get(context);
	}

	@Override
	public int count() {
		return count;
	}

	@Override
	public Kind kind() {
		if (this.eventName == "OVERFLOW") {
			return new StdWatchEventKind<Object>("OVERFLOW", Object.class);
		}
		return new StdWatchEventKind<Path>("ENTRY_CREATE", Path.class);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	private static class StdWatchEventKind<T> implements WatchEvent.Kind<T> {
		private final String name;
		private final Class<T> type;

		StdWatchEventKind(String name, Class<T> type) {
			this.name = name;
			this.type = type;
		}

		@Override
		public String name() {
			return name;
		}

		@Override
		public Class<T> type() {
			return type;
		}

		@Override
		public String toString() {
			return name;
		}
	}

}
