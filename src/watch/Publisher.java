package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;
import java.util.ArrayList;

import com.google.gson.Gson;

public class Publisher {
	ExchangeManager exchangeManager;

	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		exchangeManager = new ExchangeManager();
	}

	/* Publish notification to exchange */
	public boolean publish(
			ArrayList<SerializableFileEvent> serializableFileEvents, Path dir)
			throws IOException {
		Gson gson = new Gson();
		String jsonized = gson.toJson(serializableFileEvents);
		if (exchangeManager.sendPassively(exchangeNameForPath(dir), jsonized
				.getBytes())) {

			NotificationServer.log(String.format(
					"Server says: I sent %s --> %s in thread %d\n", jsonized,
					exchangeNameForPath(dir), Thread.currentThread().getId()));

		} else {
			NotificationServer.log("*Server Publish failed");
		}
		return true;
	}

	public String exchangeNameForPath(Path path) {
		return path.resolve(path).toString();
		// String exchangeName = path.resolve((Path) cast(event).context())
		// .toString();
	}

}
