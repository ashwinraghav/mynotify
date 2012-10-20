package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 1;

	public static void main(String a[]) {
		Pool pool = new Pool(poolSize);
		try {

			for (int i = 0; i <= 1000; i++) {
				pool.watch("/localtmp/dump/" + i, false);
			}
			
			pool.stopWatching("/localtmp/dump/303");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.shutdown();
	}
}