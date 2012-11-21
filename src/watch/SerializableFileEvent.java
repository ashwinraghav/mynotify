package watch;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.util.ArrayList;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class SerializableFileEvent implements WatchEvent {
	String eventName;
	String context;
	String originatingDirectory;
	int count;

	public SerializableFileEvent(WatchEvent<?> event, Path dir) {
		eventName = event.kind().name();
		context = event.context().toString();
		count = event.count();
		originatingDirectory = dir.toString();
	}

	protected SerializableFileEvent(String eventName, String context, int count) {
		this.eventName = eventName;
		this.context = context;
		this.count = count;
	}

	public static SerializableFileEvent constructFromJson(String jsonized) {
		return new Gson().fromJson(jsonized, SerializableFileEvent.class);
	}

	public static ArrayList<SerializableFileEvent> constructFromJsonArray(String jsonized) {
		Gson gson = new Gson();
		JsonParser parser = new JsonParser();
		JsonArray array = parser.parse(jsonized).getAsJsonArray();
		
		ArrayList<SerializableFileEvent> watchEvents = new ArrayList<SerializableFileEvent>();
		
		for(JsonElement s:array){
			watchEvents.add(gson.fromJson(s, SerializableFileEvent.class));
		}
		return watchEvents;
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
		return new StdWatchEventKind<Path>(this.eventName, Path.class);
	}

	public String toJson() {
		return new Gson().toJson(this);
	}

	protected static class StdWatchEventKind<T> implements WatchEvent.Kind<T> {
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
