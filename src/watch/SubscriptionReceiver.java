package watch;

import java.io.IOException;
/* To Do
 * 1. Start receiving subscriptions via some RPC/async mechanism
 * 
 */
public class SubscriptionReceiver {
	final static int poolSize = 10;

	public static void main(String args[]) throws IOException {

		// If no argument is passed; use testing default
		if (args.length == 0) {
			args = new String[1];
			args[0] = "/localtmp/dump/";
		}

		PoolManager pool = new PoolManager(poolSize);

		try {
			for (int i = 0; i <= 1000; i++) {
				pool.watch(args[0] + i, false);
			}
			pool.stopWatching("/localtmp/dump/303");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.shutdown();
	}
}