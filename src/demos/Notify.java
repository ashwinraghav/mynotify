package demos;

import net.contentobjects.jnotify.JNotify;
import net.contentobjects.jnotify.JNotifyListener;

public class Notify {
	public static void main(String a[]) throws Exception {
		// path to watch
		String path = System.getProperty("~/Desktop");

		// watch mask, specify events you care about,
		// or JNotify.FILE_ANY for all events.
		int mask = JNotify.FILE_CREATED | JNotify.FILE_DELETED
				| JNotify.FILE_MODIFIED | JNotify.FILE_RENAMED;
		// watch subtree?
		boolean watchSubtree = true;

		// add actual watch
		Listener l = new Listener();
		
		int watchID = JNotify.addWatch(path, mask, watchSubtree, l);

		// sleep a little, the application will exit if you
		// don't (watching is asynchronous), depending on your
		// application, this may not be required
		Thread.sleep(1000000);

		// to remove watch the watch
		boolean res = JNotify.removeWatch(watchID);
		if (!res) {
			// invalid watch ID specified.
		}
	}

}

class Listener implements JNotifyListener {
	public void fileRenamed(int wd, String rootPath, String oldName,
			String newName) {
		print("renamed " + rootPath + " : " + oldName + " -> " + newName);
	}

	public void fileModified(int wd, String rootPath, String name) {
		print("modified " + rootPath + " : " + name);
	}

	public void fileDeleted(int wd, String rootPath, String name) {
		print("deleted " + rootPath + " : " + name);
	}

	public void fileCreated(int wd, String rootPath, String name) {
		print("created " + rootPath + " : " + name);
	}

	void print(String msg) {
		System.err.println(msg);
	}
}
