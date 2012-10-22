package watch;

import java.io.IOException;
/* To Do
 * 1. Start receiving subscriptions via some RPC/async mechanism
 * 
 */
public class SubscriptionReceiver {
	final static int poolSize = 10;

	/* the main method is for testing. All subscriptions are sent 
	 * to the subscribeTo method*/
	public static void main(String args[]) throws IOException {

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
	
	public static void subscribeTo(String args) throws IOException {

		PoolManager pool = new PoolManager(poolSize);

		try {
			pool.watch(args, false);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}