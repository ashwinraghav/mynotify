package watch;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

import java.util.logging.FileHandler;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/*
 * Server Class; spawns threadpools and handles notifications
 */
public class NotificationServer {
	final static int poolSize = 1;
	static PoolManager pool;
	static Logger log;
	
	public static void log(String msg){
		log.info(msg);
	}

	/*
	 * Main Class; starts server
	 */
	public static void main(String[] args) {
		
		/*
		 * Set up logger
		 */
		log = Logger.getLogger("NotificationServer");
		try {
			FileHandler fh = new FileHandler("NotificationServer.log");
			log.addHandler(fh);
			SimpleFormatter formatter = new SimpleFormatter();
			fh.setFormatter(formatter);
		} catch (SecurityException e1) {
			System.err.println("Security Exception: "+e1.getMessage());
			System.exit(-1);
		} catch (IOException e1) {
			System.err.println("Cannot open Log File: "+e1.getMessage());
			System.exit(-1);
		}
		
		
		try {
			System.out.println("Starting XML-RPC Server...");
			log("Starting XML-RPC Server...");
			pool = new PoolManager(poolSize);
			bootstrapServer(NotificationServer.class);
		} catch (Exception e) {
			log("Server error: " + e);
			System.out.println("Failed bootstrap: "+e.getMessage());
		}
	}

	/*
	 * Starts WebServer and XML RPC Server
	 */
	private static void bootstrapServer(Class serverClass)
			throws XmlRpcException, IOException {
		WebServer webserver = new WebServer(Constants.serverPort);
		XmlRpcServer server = webserver.getXmlRpcServer();
		PropertyHandlerMapping map = new PropertyHandlerMapping();
		map.addHandler("HandlerClass", serverClass);
		server.setHandlerMapping(map);

		System.out.println("Server ready to handle requests");
		log("Server ready to handle requests");
		webserver.start();
	}

	/*
	 * Register method registers a directory
	 */
	public String register(String a) {

		try {
			pool.watch(a, false);
		}catch(java.nio.file.NoSuchFileException e){
			System.err.println("Client requested non-existent path: "+e.getLocalizedMessage());
			log("Client requested non-existent path: "+e.getLocalizedMessage());
			return "0: File does not exist on the server: "+a;
		}
		catch (IOException e) {
			e.printStackTrace();
		}
		log("Client registered: "+a);
		return "1: Registered: " + a;
	}
}