package watch;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.client.XmlRpcClient;
import org.apache.xmlrpc.client.XmlRpcClientConfigImpl;


public class RPCManager {
	XmlRpcClient client;
	
	/**
	 * Return RPC Client bound to passed hostname
	 * @param serverHostName
	 * @throws MalformedURLException
	 */
	public RPCManager(String serverHostName) throws MalformedURLException {
		client = getRPCClient(serverHostName);
	}

	//Create new XmlRpcClient and set hostname to passed serverHostName value
	private static XmlRpcClient getRPCClient(String serverHostName) throws MalformedURLException {
		XmlRpcClientConfigImpl config = new XmlRpcClientConfigImpl();
		config.setServerURL(new URL(serverHostName));
		XmlRpcClient client = new XmlRpcClient();
		client.setConfig(config);
		return client;
	}
	
	/**
	 * Execute the methodName with the given parameters synchronously and return String value
	 * @param methodName
	 * @param parameters
	 * @return
	 * @throws XmlRpcException
	 */
	public String execute(String methodName, Object[] parameters) throws XmlRpcException{
		return (String)client.execute(methodName, parameters);
	}

}
