package watch;

public class ExampleApplication {

	public static void main(String[] args) {
		String directory = "/if8/am2qa/temp";
		
		//Allow user to pass name of directory as command line argument
		if(args.length == 1)
			directory = args[0];
		
		MyFSWatcher cs = new MyFSWatcher();
		cs.subscribe(directory);
	}

}
