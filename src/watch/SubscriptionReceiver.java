package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 50;

	public static void main(String a[]) {
		Pool pool = new Pool(poolSize);
		try {

			for (int i = 0; i <= 1000; i++) {
				//System.out.println("/localtmp/dump/" + i);
				pool.watch("/localtmp/dump/" + i, false);
			}
			// pool.stopWatching("/if8/am2qa/Desktop");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.close();
	}
}