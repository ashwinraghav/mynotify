package watch;

import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_MODIFY;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.file.WatchEvent;
import java.nio.file.WatchEvent.Kind;
import java.util.ArrayList;
import java.util.Arrays;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.QueueingConsumer;

/**
 * Class used for notification services
 */
public class ClientSubscriber {
	
	//ArrayList of NFS mountpoints
	private static ArrayList<String> mountedDirectories;

	/**
	 * Default Subscriber Constructor
	 */
	public ClientSubscriber(){
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
			System.out.println("ERROR: Unknown OS");
			System.exit(-1);
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
			e.printStackTrace();
			System.exit(-1);
		}
	}
	
	private static void subscribe(String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes){

		for(int i = 0; i < mountedDirectories.size(); i++){
			if(dirName.startsWith(mountedDirectories.get(i))){
				System.out.println("Remote Directory");
				nfssubscribe(dirName, durable, ENTRY_CREATE, ENTRY_DELETE,
						ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);
			}
		}
		System.out.println("Local Directory");
		nfssubscribe(dirName, durable, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);
	}
	

	/*
	 * Method for subscribing to a directory that is distributed
	 */	
	private static void nfssubscribe(String dirName, boolean durable,
			WatchEvent.Kind<?>... subscriptionTypes) {

		try {
			boolean autoDelete = true;
			RPCManager rpcManager = new RPCManager();

			Object[] parameters = new Object[] { new String(dirName) };
			String i = (String) rpcManager.execute("HandlerClass.register",
					parameters);
			
			if(i.startsWith("0: ")){
				System.err.println("Registration Failed");
				return;
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
		} catch(java.net.MalformedURLException e){
			System.err.println("Malformed URL Exception"+e.getLocalizedMessage());
		} catch(java.io.IOException e){
			System.err.println("IOException: "+e.getLocalizedMessage());
		} catch(com.rabbitmq.client.ShutdownSignalException e){
			System.err.println("Shutdown Signal Sent: "+e.getLocalizedMessage());
		} catch(com.rabbitmq.client.ConsumerCancelledException e){
			System.err.println("Consumer has cancelled: "+e.getLocalizedMessage());
		} catch(Exception e){
			System.err.println("Exception: "+e.getLocalizedMessage());
		}
	}

	public static void main(String[] a) {
		boolean durable = false;
		boolean isNFS = false;
		String directory = "/if8/am2qa/temp";
		ArrayList<String> mountedDirectories = new ArrayList<String>();
		
		//Allow user to pass name of directory as command line argument
		if(a.length == 1)
			directory = a[0];
		
		ClientSubscriber cs = new ClientSubscriber();
		ClientSubscriber.subscribe(directory, durable, ENTRY_CREATE, ENTRY_DELETE,
				ENTRY_MODIFY, NotificationStopEvent.NOTIFICATION_STOP);

	}

}
