package watch;

import java.nio.file.WatchKey;
import java.util.Date;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Class used to keep track of the number of notifications in a <thresholdTime> period of time
 * It is used to determine if successive notifications are part of a Burst
 */

public class BurstController {
	
	private ConcurrentHashMap<WatchKey, NotificationTimeStamp> notificationTimeStamps;
	private  static long thresholdCount = 10;
	private  static long thresholdTime = 5 * 1000; // milliseconds

	/*
	 * Default Constructor for BurstController
	 */
	public BurstController() {
		this.notificationTimeStamps = new ConcurrentHashMap<WatchKey, NotificationTimeStamp>();
	}

	/*
	 * Parameterized Constructor for BurstController
	 */
	public BurstController(long thresholdCount, long thresholdTime){
		this.notificationTimeStamps = new ConcurrentHashMap<WatchKey, NotificationTimeStamp>();
		this.thresholdCount = thresholdCount;
		this.thresholdTime = thresholdTime;
	}
	
	/*
	 * This function returns whether or not a BURST has occurred.
	 */
	public String checkBurst(WatchKey key) {
		
		long currentTime = new Date().getTime();
		NotificationTimeStamp timeStamp = notificationTimeStamps.get(key);
		
		//There is no timeStamp for the current WatchKey; set to current time
		if (timeStamp == null) {
			notificationTimeStamps.put(key, new NotificationTimeStamp());
			return "NO";
		}
		
		/*If there are more than <thresholdCount> notifications for the same path
		 * in <thresholdTime> seconds, return "YES"
		 * else return "NO"
		 */
		if ((currentTime - timeStamp.getSentTime()) < thresholdTime) {
			timeStamp.incrementCount();
			
			if((notificationTimeStamps.get(key).getNotificationCount()) > thresholdCount){
				return "YES";
			}else{
				return "NO";
			}
		} else {
			timeStamp.update();
			return "NO";
		}
	}
}
