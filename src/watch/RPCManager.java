package watch;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;

public class RPCManager {
	XmlRpcClient client;
	public RPCManager() throws MalformedURLException {
		client = getRPCClient();
	}
	
	public static XmlRpcClient getRPCClient() throws MalformedURLException {
		XmlRpcClientConfigImpl config;
		XmlRpcClient client;
		config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(Constants.serverUrl));

		client = new XmlRpcClient();
		client.setConfig(config);
		return client;
	}
	public String execute(String methodName, Object[] parameters) throws XmlRpcException{
		return (String)client.execute(methodName, parameters);
	}

}
