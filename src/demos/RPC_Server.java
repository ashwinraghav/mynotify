package demos;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.server.XmlRpcServerConfigImpl;
import org.apache.xmlrpc.webserver.WebServer;

public class RPC_Server {
	
	public static void main(String[] args) {
		//Web server port
		final int port = 8080;
		
		try{
			System.out.println("Starting XML-RPC Server");
			
			//Web server on port <port>
			WebServer webserver = new WebServer(port);
			XmlRpcServer server = webserver.getXmlRpcServer();
			PropertyHandlerMapping map = new PropertyHandlerMapping();
			
			//Map handler name to handler class
			map.addHandler("HandlerClass", RPC_Server.class);
			server.setHandlerMapping(map);
			
			webserver.start();
		}catch(Exception e){
			System.err.println("Server error: "+e);
		}
	}
	
	public Integer sum(int a, int b, int c){
		System.out.println("Running Handler!");
		return new Integer(a+b+c);
	}
}