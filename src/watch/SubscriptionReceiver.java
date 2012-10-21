package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 5;

	public static void main(String args[]) throws IOException {

		// If no argument is passed; use testing default
		if (args.length == 0) {
			args = new String[1];
			args[0] = "/localtmp/dump/";
		}

		Pool pool = new Pool(poolSize);

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