package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 1;

	public static void main(String args[]) {
		
		//If no argument is passed; use testing default
		if(args.length == 0){
			args[0] = new String();
			args[0] = "/localtmp/dump";
		}
		
		Pool pool = new Pool(poolSize);
		
		try {
			for (int i = 0; i <= 1000; i++) {
<<<<<<< HEAD
				pool.watch("/localtmp/dump/" + i, false);
=======
				pool.watch(args[0] + i, false);		
>>>>>>> bf3fbf4a3816f53742f27e99861d773f6459e9ee
			}
			
			pool.stopWatching("/localtmp/dump/303");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.shutdown();
	}
}