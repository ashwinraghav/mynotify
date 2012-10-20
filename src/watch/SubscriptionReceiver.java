package watch;

import java.io.IOException;

public class SubscriptionReceiver {
	final static int poolSize = 50;

	public static void main(String args[]) {
		
		//If no argument is passed; use testing default
		if(args.length == 0){
			args[0] = new String();
			args[0] = "/localtmp/dump";
		}
		
		Pool pool = new Pool(poolSize);
		
		try {
			for (int i = 0; i <= 1000; i++) {
				pool.watch(args[0] + i, false);		
			}
			// pool.stopWatching("/if8/am2qa/Desktop");
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		pool.close();
	}
}