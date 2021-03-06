package demos;
import java.net.URL;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class RPC_Client {
		
	public static void main(String[] args) {
		
		//Server URL and port
		String serverUrl = "http://localhost:8080";
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		
		try{
			config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(serverUrl));
			
			client = new XmlRpcClient();
			client.setConfig(config);
			
			//Parameters to be pushed to RPC Server handler
			Object[] parameters = new Object[]{new Integer(2), new Integer(4), new Integer(6)};
			
			Integer i = (Integer)client.execute("HandlerClass.sum", parameters);
			System.out.println("Returned: "+i);
			
		}catch(Exception e){
			System.out.println("RPC_Client: "+e);
		}
	}

}
