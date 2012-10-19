package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 1;

	public static void main(String a[]) {
		Pool pool = new Pool(poolSize);
		try {
			pool.startWatching("/if8/am2qa/Desktop", false);
			pool.startWatching("/localtmp/", false);
			//pool.stopWatching("/if8/am2qa/Desktop");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.close();
	}
}


