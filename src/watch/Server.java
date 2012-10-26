package watch;
import java.io.IOException;

import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Server {
	final static int poolSize = 1;
	static PoolManager pool;
	
	public static void main(String[] args) {
		//Web server port
		final int port = 8080;
		
		try{
			System.out.println("Starting XML-RPC Server");
			pool = new PoolManager(poolSize);
						
			//Web server on port <port>
			WebServer webserver = new WebServer(port);
			XmlRpcServer server = webserver.getXmlRpcServer();
			PropertyHandlerMapping map = new PropertyHandlerMapping();
			
			//Map handler name to handler class
			map.addHandler("HandlerClass", Server.class);
			server.setHandlerMapping(map);
			
			
			System.out.println("Server ready to handle requests");
			
			webserver.start();
		}catch(Exception e){
			System.err.println("Server error: "+e);
		}
	}
	
	public String register(String a){
		
		try {
			pool.watch(a, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Pong: "+a;
	}
}