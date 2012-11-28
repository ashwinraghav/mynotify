package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.WatchEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * Class used for notification services
 */
public class MyFSWatcher {

	private HashMap<String, String> mounted;
	private LocalWatcher lw;
	private DistributedWatcher dw;
	private final int port = 8080;
	private boolean testingDistributed = true;
	private boolean debug = true;

	/**
	 * Default Subscriber Constructor
	 */
	public MyFSWatcher() {
		mounted = new HashMap<String, String>();
		lw = null;
		dw = null;
		setMountPoints();
	}

	/**
	 * Populates mountedDirectories array with mount point locations
	 */
	private void setMountPoints() {

		String os = System.getProperty("os.name").toLowerCase();
		String searchString = null;

		
		if (os.contains("mac")) {
			searchString = "(nfs";
		} else if ((os.contains("nix") || (os.contains("linux")))) {
			searchString = "type nfs";
		} else {
			System.err.println("ERROR: Unknown OS");
			return;
		}

		if(debug)
			System.out.println("Searching local OS for NFS directories...");
		
		String line;
		try {
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("mount");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p
					.getInputStream()));
			ArrayList<String> mount_info = new ArrayList<String>();

			while ((line = b.readLine()) != null) {
				mount_info.add(line);
			}

			for (int i = 0; i < mount_info.size(); i++) {
				if (mount_info.get(i).contains(searchString)) {
					ArrayList<String> splitLine = new ArrayList<String>(Arrays
							.asList(mount_info.get(i).split(" ")));

					String hostName = splitLine.get(0).split(":")[0];

					int indexOfPath = splitLine.indexOf("on");
					String mountPoint = splitLine.get(++indexOfPath);
					//Format hostname and add port
					if(debug)
						System.out.println("Adding NFS entry; Hostname: "+hostName+", Mountpoint:"+mountPoint);
					mounted.put(mountPoint, "http://"+hostName+":"+port);
				}
			}
		} catch (Exception e) {
			System.out
					.println("Error running `mount` command; Only Linux and OSX are currently supported");
			System.exit(-1);
		}
	}

	/**
	 * Subscribe to a directory
	 * @param dirName
	 * @return 0 if local file is registered; 1 if distributed file is
	 *         registered Doesn't actually return though
	 */
	public int subscribe(String dirName) {
		
		boolean durable = false;
		Iterator<Map.Entry<String, String>> iter = mounted.entrySet().iterator();
		String temphost;
		
		if(testingDistributed){
			//Subscribe to nfs directory; default to all notification types
			nfssubscribe(Constants.serverUrl, dirName, durable, ENTRY_CREATE,
					ENTRY_DELETE, ENTRY_MODIFY,
					NotificationStopEvent.NOTIFICATION_STOP);
			return 1;
		}

		while (iter.hasNext()) {
			Map.Entry<String,String> pairs = iter.next();
			if (dirName.startsWith((String) pairs.getKey())) {
				temphost = (String) pairs.getValue();
				if(debug)
					System.out.println("NFS subscription for: " + temphost);
				//Subscribe to nfs directory; default to all notification types
				nfssubscribe(temphost, dirName, durable, ENTRY_CREATE,
						ENTRY_DELETE, ENTRY_MODIFY,
						NotificationStopEvent.NOTIFICATION_STOP);
				return 1;
			}
		}
		
		//If not distributed, use local
		//Subscribe to local directory; default to all notification types
		if(debug)
			System.out.println("Local subscription for: "+dirName);
		
		localsubscribe(dirName, durable, ENTRY_CREATE,ENTRY_DELETE,ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);
		return 0;
	}

	/*
	 * Method for subscribing to a directory that is distributed
	 */
	private int nfssubscribe(String hostName, String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes) {
		if (dw == null) {
			try {
				dw = new DistributedWatcher(hostName, durable);
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		try {
			dw.register(dirName, subscriptionTypes);
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		return 0;
	}

	/*
	 * Method for subscribing to a directory that is local
	 */
	private int localsubscribe(String dirName, boolean durable, WatchEvent.Kind<?>... subscriptionTypes) {
		
		//If a local watcher has not been instantiated yet, create one
		if (this.lw == null) {
			try {
				this.lw = new LocalWatcher();
			} catch (IOException e) {
				e.printStackTrace();
				return -1;
			}
		}
		//Register the passed directory name
		try {
			this.lw.register(dirName, subscriptionTypes);
		} catch (IOException e) {
			System.err.println("Could not locally register: " + dirName);
			return -1;
		}
		return 0;
	}

	/**
	 * Poll for events
	 * @return ArrayList of events
	 * @throws InterruptedException
	 */
	public ArrayList<SerializableFileEvent> pollEvent() throws InterruptedException {
		
		ArrayList<SerializableFileEvent> temp;
		ArrayList<SerializableFileEvent> messages = new ArrayList<SerializableFileEvent>();

		//Poll distributed watcher first
		if (dw != null) {
			temp = dw.pollEvents();
			if (temp != null) {
				messages.addAll(temp);
			}
		}
		//Then poll local watcher
		if (lw != null) {
			temp = lw.pollEvents();
			if (temp != null) {
				messages.addAll(temp);
			}
		}
		return messages;
	}
}
