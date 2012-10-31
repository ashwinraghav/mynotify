package watch;

import java.nio.file.Path;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;

/*these are the entities that the manager cleansup*/
public class CleanupManager {

	ArrayList<WatchService> watchers;
	ArrayList<ConcurrentHashMap<WatchKey, Path>> keys;
	HashMap<String, WatchService> file_subscriptions;
	
	CleanupManager(	ArrayList<WatchService> watchers, ArrayList<ConcurrentHashMap<WatchKey, Path>> keys,HashMap<String, WatchService> file_subscriptions){
		this.watchers = watchers;
		this.file_subscriptions = file_subscriptions;
		this.keys = keys;
	}
	private void cleanExchanges(){
		
	}
	
	private void cleanSubscritions(){
		
	}

}
