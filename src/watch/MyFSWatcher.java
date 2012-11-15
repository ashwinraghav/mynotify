package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Arrays;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Class used for notification services
 */
public class MyFSWatcher {
	
	//ArrayList of NFS mountpoints
	private static ArrayList<String> mountedDirectories;

	/**
	 * Default Subscriber Constructor
	 */
	public MyFSWatcher(){
		mountedDirectories = new ArrayList<String>();
		setMountPoints();
	}
	
	/**
	 * Populates mountedDirectories array with mount point locations
	 */
	private void setMountPoints(){
		
		String os = System.getProperty("os.name").toLowerCase();
		String searchString = null;
		
		if(os.contains("mac")){
			searchString = "(nfs";
		}
		else if((os.contains("nix") || (os.contains("linux")))){
			searchString = "type nfs";
		}
		else{
			System.err.println("ERROR: Unknown OS");
			return;
		}
		
		String line;
		try{
			Runtime r = Runtime.getRuntime();
			Process p = r.exec("mount");
			p.waitFor();
			BufferedReader b = new BufferedReader(new InputStreamReader(p.getInputStream()));
			ArrayList<String> mount_info = new ArrayList<String>();
			
			while((line = b.readLine()) != null){
				mount_info.add(line);
			}

			for(int i = 0; i < mount_info.size(); i++){
				if(mount_info.get(i).contains(searchString)){
				  ArrayList<String> splitLine = new ArrayList<String>(Arrays.asList(mount_info.get(i).split(" ")));
				  int indexOfPath = splitLine.indexOf("on");
				  String mountPoint = splitLine.get(++indexOfPath);
				  mountedDirectories.add(mountPoint);
				}
			}

		}catch(Exception e){
			System.out.println("Error running `mount` command; Only Linux and OSX are currently supported");
			System.exit(-1);
		}
	}
	
	/**
	 * 
	 * @param dirName
	 * @return 0 if local file is registered; 1 if distributed file is registered
	 * Doesn't actually return though
	 */
	public int subscribe(String dirName){
		boolean durable = false;

		for(int i = 0; i < mountedDirectories.size(); i++){
			if(dirName.startsWith(mountedDirectories.get(i))){
				System.out.println("Remote Directory Subscription for: "+dirName);
				nfssubscribe(dirName, durable, ENTRY_CREATE, ENTRY_DELETE,
						ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);
				return 1;
			}
		}
		System.out.println("Local Directory Subscription for: "+dirName);
		localsubscribe(dirName, durable, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);
		return 0;
	}
	

	/*
	 * Method for subscribing to a directory that is distributed
	 */	
	private int nfssubscribe(String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes) {

		try {
			boolean autoDelete = true;
			RPCManager rpcManager = new RPCManager();

			Object[] parameters = new Object[] { new String(dirName) };
			String i = (String) rpcManager.execute("HandlerClass.register",
					parameters);
			
			if(i.startsWith("0: ")){
				System.err.println("Registration Failed");
				return -1;
			}
			else if(i.startsWith("1: ")){
				System.out.println("Registration Successful");
				System.out.println(i);
			}

			ExchangeManager exchangeManager = new ExchangeManager();
			Channel channel = exchangeManager.createChannel();
			exchangeManager.declareExchange(channel, dirName,
					Constants.exchangeMap);

			// bind all subscriptions
			if (durable) {
				autoDelete = false;
			}
			String queueName = channel.queueDeclare("", durable,
					false, autoDelete, null).getQueue();
			for (Kind<?> w : subscriptionTypes) {
				channel.queueBind(queueName, dirName, w.name());
			}
			// *****

			System.out.println(" [*] Waiting for messages.");
			QueueingConsumer consumer = new QueueingConsumer(channel);
			channel.basicConsume(queueName, true, consumer);

			while (true) {
				QueueingConsumer.Delivery delivery = consumer.nextDelivery();
				String jsonizedMessage = new String(delivery.getBody());
				
				System.out.println(" [x] Received: " + jsonizedMessage);
			}
		} catch (org.apache.xmlrpc.XmlRpcException e) {
			System.err.println("XML RPC Exception: "+e.getLocalizedMessage());
			return -1;
		} catch(java.net.MalformedURLException e){
			System.err.println("Malformed URL Exception"+e.getLocalizedMessage());
			return -1;
		} catch(java.io.IOException e){
			System.err.println("IOException: "+e.getLocalizedMessage());
			return -1;
		} catch(com.rabbitmq.client.ShutdownSignalException e){
			System.err.println("Shutdown Signal Sent: "+e.getLocalizedMessage());
			return -1;
		} catch(com.rabbitmq.client.ConsumerCancelledException e){
			System.err.println("Consumer has cancelled: "+e.getLocalizedMessage());
			return -1;
		} catch(Exception e){
			System.err.println("Exception: "+e.getLocalizedMessage());
			return -1;
		}
	}
	
	/*
	 * Method for subscribing to a directory that is local
	 */	
	private int localsubscribe(String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes){
		LocalWatcher lw;
		try {
			lw = new LocalWatcher();
		} catch (IOException e) {
			e.printStackTrace();
			return -1;
		}
		try {
			lw.register(Paths.get(dirName));
		} catch (IOException e) {
			System.err.println("Could not register; "+dirName);
			return -1;
		}
		
		lw.processEvents();
		return 0;
	}
}
