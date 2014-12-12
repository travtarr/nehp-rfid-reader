package com.nehp.rfid_system.reader.server_connection;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingWorker;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.json.JSONArray;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.impl.SimpleLogger;
import org.slf4j.impl.SimpleLoggerFactory;

import com.nehp.rfid_system.reader.windows.MainWindow;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This class provides the necessary methods to send and receive data to/from the server.
 * @author Travis Tarr
 *
 */
public class Server {
	
	private final SimpleLoggerFactory factory = new SimpleLoggerFactory();
	private final Logger LOG = factory.getLogger("Server");
	
	private final String GRANT_TYPE = "password";
	
	private String address = null;
	private String user = null;
	private String password = null;
	
	private String accessToken = null;
	private MainWindow mainWindow;
	
	public Server(String address, String user, String password, MainWindow mainWindow){
		this.address = address;
		this.user = user;
		this.password = password;
		this.mainWindow = mainWindow;
	}

	/**
	 * Logs the client into the server.
	 * @param progressBar - updates the given progress bar
	 * @return
	 */
	public boolean login(ProgressBar progressBar){
	
		if( !progressBar.isDisposed() ) {
			
			if( !hasInternetConnection() ){
				LOG.info("No internet connection");
				return false;
			}
			progressBar.setEnabled(true);
			progressBar.setSelection(5);
			
			Client client = new Client();

			MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
			formData.add("grant_type", GRANT_TYPE);
			formData.add("username", user);
			formData.add("password", password);
			
			progressBar.setSelection(25);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			
			ClientResponse response = client
					.resource(address + "/auth/token")
					.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.post(ClientResponse.class, formData);
			
			progressBar.setSelection(50);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			
			if(response.getStatus() != 200){
				LOG.warn("Bad Response: {}", response.getStatus());
				return false;
			}
			
			LOG.info("Good response");
			
			JSONObject obj = new JSONObject(response.getEntity(String.class));
			JSONArray array = obj.getJSONArray("api_key");
			
			progressBar.setSelection(75);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			
			accessToken = array.getJSONObject(0).getJSONObject("accessToken").getString("string");

			progressBar.setSelection(100);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
						
			LOG.info("Connected with token: [{}]", accessToken);
		}
	
		progressBar.setVisible(false);
		
		LOG.info("Login executed");
		return true;
	}
	
	
	public boolean sendNextStage(Long item) {
		String service = "/service/item/nextstage";
		JSONObject obj = new JSONObject();
		obj.append("id", item);
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return true;
		}
		
		return false;
	}
	
	public boolean sendNextStage(Long[] list){
		String service = "/service/items/nextstage";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		obj.append("list", array);
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return true;
		}
		
		return false;
	}
	
	public boolean sendStage(Long item, int stage){
		String service = "/service/item/sendstage";
		JSONObject obj = new JSONObject();
		obj.append("id", item);
		obj.append("stage", stage);
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return true;
		}
		
		return false;
	}
	
	public boolean sendStage(Long[] list, int stage){
		String service = "/service/items/sendstage";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		obj.append("list", array);
		obj.append("stage", stage);
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return true;
		}
		
		return false;
	}
	
	public JSONObject getItem(Long item){
		String service = "/service/item/";
		ClientResponse response = get(service, item);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return response.getEntity(JSONObject.class);
		}
		
		return null;
	}
		
	public JSONObject getItems(Long[] list){
		String service = "/service/item/";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		
		obj.append("list", array);
		
		ClientResponse response = post(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return response.getEntity(JSONObject.class);
		}
		
		return null;
	}
	
	/**
	 * Determine if server is connected.
	 * @return
	 */
	public boolean isConnected(){
		if(accessToken != null && hasInternetConnection())
			return true;
		else
			return false;
	}
	
	
	/**
	 * Base method to send a PUT request to the server.
	 * 
	 * @param service - URI of service being requested
	 * @param obj - JSONObject of the items being requested
	 * @return
	 */
	private ClientResponse put(String service, JSONObject obj){
		Client client = new Client();
		ClientResponse response = client
				.resource(address + service)
				.header("Authorization", String.format("Bearer %s", accessToken))
				.type(MediaType.APPLICATION_JSON_TYPE)
				.put(ClientResponse.class, obj);
		
		return response;
	}
	
	/**
	 * Base method to send a POST request to the server.
	 * 
	 * @param service - URI of service being requested
	 * @param obj - JSONObject of the items being requested
	 * @return
	 */
	private ClientResponse post(String service, JSONObject obj){
		Client client = new Client();
		ClientResponse response = client
				.resource(address + service)
				.header("Authorization", String.format("Bearer %s", accessToken))
				.type(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, obj);
		
		return response;
	}
	
	/**
	 * Base method to send a GET request to the server.
	 * 
	 * @param service - URI of service being requested
	 * @param obj - JSONObject of the items being requested
	 * @return
	 */
	private ClientResponse get(String service, Long item){
		Client client = new Client();
		ClientResponse response = client
				.resource(address + service + "/" + item)
				.header("Authorization", String.format("Bearer %s", accessToken))
				.type(MediaType.APPLICATION_JSON_TYPE)
				.get(ClientResponse.class);
		
		return response;
	}
		
	
	/**
	 * Determine if client's device has access to the internet.
	 * @return
	 */
	private boolean hasInternetConnection(){
		boolean connected = false;
		Socket s = null;
		try {
			s = new Socket(InetAddress.getByName("www.google.com"), 80);
			if(s.isConnected()){
				s.close();
				connected = true;
			}
		} catch (UnknownHostException e) {
			// do nothing - boolean defaulted false
			LOG.warn("unknown host");
		} catch (IOException e) {
			// do nothing - boolean defaulted false
			LOG.warn("IOexception");
		} 
		return connected;
	}
}
