package watch;

import java.io.IOException;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcServer;
import org.apache.xmlrpc.webserver.WebServer;

public class Server {
	final static int poolSize = 1;
	static PoolManager pool;

	public static void main(String[] args) {
		try {
			System.out.println("Starting XML-RPC Server");
			pool = new PoolManager(poolSize);
			bootstrapServer(Server.class);
		} catch (Exception e) {
			System.err.println("Server error: " + e);
		}
	}

	private static void bootstrapServer(Class serverClass)
			throws XmlRpcException, IOException {
		WebServer webserver = new WebServer(Constants.serverPort);
		XmlRpcServer server = webserver.getXmlRpcServer();
		PropertyHandlerMapping map = new PropertyHandlerMapping();

		map.addHandler("HandlerClass", serverClass);
		server.setHandlerMapping(map);

		System.out.println("Server ready to handle requests");
		webserver.start();
	}

	public String register(String a) {

		try {
			pool.watch(a, false);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return "Pong: " + a;
	}
}