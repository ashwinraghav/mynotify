package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.util.ArrayList;

public class ExampleApplication {

	public static void main(String[] args) throws InterruptedException {
		String directory = "/localtmp/dump";

		// Allow user to pass name of directory as command line argument
		if (args.length == 1)
			directory = args[0];

		MyFSWatcher cs = new MyFSWatcher();
		cs.subscribe(directory);
		cs.subscribe(directory + "/1");
		cs.subscribe(directory + "/2");
		cs.subscribe(directory + "/3");
		cs.subscribe(directory + "/4");

		ArrayList<SerializableFileEvent> events;

		while (true) {
			events = cs.pollEvent();
			for (int i = 0; i < events.size(); i++) {
				System.out.println(events.get(i).toJson());
			}
		}
	}
}
