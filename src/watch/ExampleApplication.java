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
		ArrayList<SerializableFileEvent> events;
		
		while(true){
			events = (ArrayList<SerializableFileEvent>) cs.pollEvent();
			for(int i = 0; i < events.size(); i++){
				System.out.println("i: "+events.get(i).eventName);
			}
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
