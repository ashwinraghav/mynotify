package watch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.WatchEvent;

public class Publisher {
	ExchangeManager exchangeManager;

	static <T> WatchEvent<T> cast(WatchEvent<?> event) {
		return (WatchEvent<T>) event;
	}

	public Publisher() throws IOException {
		exchangeManager = new ExchangeManager();
	}
	

		/*Publish notification to exchange*/
	public boolean publish(Path dir, WatchEvent<?> event) throws IOException {
		String jsonized = (new SerializableFileEvent(event, dir)).toJson();
		if (exchangeManager.sendPassively(exchangeNameForPath(dir), event
				.kind().name(), jsonized.getBytes())){
			System.out.format("Server says: I sent %s --> %s \n", jsonized,
					exchangeNameForPath(dir));
			NotificationServer.log(String.format("Server says: I sent %s --> %s \n", jsonized,
					exchangeNameForPath(dir)));
		}else{
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
