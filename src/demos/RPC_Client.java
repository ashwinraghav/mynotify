package demos;
import java.net.URL;
import java.util.*;
import org.apache.xmlrpc.*;
import org.apache.xmlrpc.client.AsyncCallback;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;


class CallBack implements AsyncCallback{
	int ret;
	boolean done;
	
	public CallBack(int ret){
		this.ret = ret;
		done = false;
	}
	
	public void handleResult(XmlRpcRequest arg0, Object arg1) {
		System.out.println("Returned value: "+ret);
	}
	
	public void handleError(XmlRpcRequest arg0, Throwable arg1) {
		System.err.println("RPC Call failed");
		System.out.println(arg0.getMethodName());
		done = true;
	}
}

public class RPC_Client {
		
	public static void main(String[] args) {
		String serverUrl = "http://localhost";
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		
		try{
			System.out.println("Starting Config");
			config = new XmlRpcClientConfigImpl();
			config.setServerURL(new URL(serverUrl));
			System.out.println("Set Server URL");
			client = new XmlRpcClient();
			client.setConfig(config);
			System.out.println("Set Client Config");
			Object[] parameters = new Object[]{new Integer(1234)};
			System.out.println("Created Parameters");
			CallBack call = new CallBack(1234);
			client.executeAsync("HandlerClass.sum",parameters,call);
			System.out.println("Executed");
			
			while(true){
			}
			
		}catch(Exception e){
			System.out.println("RPC_Client: "+e);
		}
	}

}
