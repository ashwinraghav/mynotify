package watch;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.util.Date;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

public class BurstController {
	private ConcurrentHashMap<WatchKey, NotificationTimeStamp> notificationTimeStamps;
	private final static long thresholdCount = 10;
	private final static long thresholdTime = 5 * 1000; // milliseconds

	public BurstController() {
		this.notificationTimeStamps = new ConcurrentHashMap<WatchKey, NotificationTimeStamp>();
	}

	public String checkBurst(WatchKey key) {
		long currentTime = new Date().getTime();
		if (notificationTimeStamps.get(key) == null) {
			notificationTimeStamps.put(key, new NotificationTimeStamp());
			return "NO";
		}
		if ((currentTime - notificationTimeStamps.get(key).getSentTime() < thresholdTime)) {
			notificationTimeStamps.get(key).incrementCount();
			if(notificationTimeStamps.get(key).getNotificationCount() > thresholdCount){
				return "YES";
			}else{
				return "NO";
			}
		} else {
			notificationTimeStamps.put(key, new NotificationTimeStamp());
			return "NO";
		}
	}
}
