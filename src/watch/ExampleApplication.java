package watch;
import java.util.ArrayList;

public class ExampleApplication {

	public static void main(String[] args) throws InterruptedException {
		
		//Directory to be subscribed to
		String directory = "/localtmp/dump";

		// Allow user to pass name of directory as command line argument
		if (args.length == 1)
			directory = args[0];

		//Constract MyFSWatcher
		MyFSWatcher cs = new MyFSWatcher();
		
		//Subscribe to directory and 4 additional subdirectories
		cs.subscribe(directory);
		cs.subscribe(directory + "/1");
		cs.subscribe(directory + "/2");
		cs.subscribe(directory + "/3");
		cs.subscribe(directory + "/4");

		//Continunously poll the watcher for new notifications and handle them
		ArrayList<SerializableFileEvent> events;
		while (true) {
			events = cs.pollEvent();
			for (int i = 0; i < events.size(); i++) {
				System.out.println(events.get(i).toJson());
			}
		}
	}
}
