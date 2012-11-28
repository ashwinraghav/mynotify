package watch;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class RPCManager {
	XmlRpcClient client;
	public RPCManager(String serverHostName) throws MalformedURLException {
		client = getRPCClient(serverHostName);
	}
	
	public static XmlRpcClient getRPCClient(String serverHostName) throws MalformedURLException {
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		config = new XmlRpcClientConfigImpl();
		System.out.println(serverHostName);
		config.setServerURL(new URL(serverHostName));

		client = new XmlRpcClient();
		client.setConfig(config);
		return client;
	}
	public String execute(String methodName, Object[] parameters) throws XmlRpcException{
		return (String)client.execute(methodName, parameters);
	}

}
