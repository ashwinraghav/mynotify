package watch;

import java.util.Date;

public class NotificationTimeStamp {
	private long sentTime;
	private long notificationCount;

	public long getSentTime() {
		return sentTime;
	}

	public void setSentTime(long sentTime) {
		this.sentTime = sentTime;
	}

	public long getNotificationCount() {
		return notificationCount;
	}

	public void setSentCount(long sentCount) {
		this.notificationCount = sentCount;
	}

	public NotificationTimeStamp() {
		this.sentTime = new Date().getTime();
		this.notificationCount = 0;
	}

	public void incrementCount() {
		this.notificationCount += 1;
	}

}
