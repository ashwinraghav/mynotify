package watch;

import java.util.ArrayList;

public class ExampleApplication {

	public static void main(String[] args) {
		String directory = "/if8/am2qa/temp";
		
		//Allow user to pass name of directory as command line argument
		if(args.length == 1)
			directory = args[0];
		
		MyFSWatcher cs = new MyFSWatcher();
		cs.subscribe(directory);
		cs.subscribe(directory+"/1");
		cs.subscribe(directory+"/2");
		ArrayList<SerializableFileEvent> events;
		
		while(true){
			events = cs.pollEvent();
			for(int i = 0; i < events.size(); i++){
				System.out.println(events.get(i).toJson());
			}
			try {
				Thread.sleep(100);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
}
