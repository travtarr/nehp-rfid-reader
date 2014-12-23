package com.nehp.rfid_system.reader.server_connection;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.swing.SwingWorker;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.MultivaluedMap;

import org.eclipse.swt.widgets.ProgressBar;
import org.eclipse.swt.widgets.Shell;
import org.glassfish.jersey.media.multipart.FormDataMultiPart;
import org.glassfish.jersey.media.multipart.file.FileDataBodyPart;
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

	
	public Server(String address, String user, String password){
		this.address = address;
		this.user = user;
		this.password = password;;
	}

	/**
	 * Logs the client into the server.
	 * @return
	 */
	public boolean login(){

		if( !hasInternetConnection() ){
			LOG.info("No internet connection");
			return false;
		}
		
		Client client = new Client();

		MultivaluedMap<String, String> formData = new MultivaluedMapImpl();
		formData.add("grant_type", GRANT_TYPE);
		formData.add("username", user);
		formData.add("password", password);
		
		ClientResponse response = client
				.resource(address + "/auth/token")
				.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.post(ClientResponse.class, formData);
		
		if(response.getStatus() != 200){
			LOG.warn("Bad Response: {}", response.getStatus());
			return false;
		}
		
		LOG.info("Good response");
		
		JSONObject obj = new JSONObject(response.getEntity(String.class));
		JSONArray array = obj.getJSONArray("api_key");

		accessToken = array.getJSONObject(0).getJSONObject("access_token").getString("string");
		accessToken = "Bearer " + accessToken;
					
		LOG.info("Connected with token: [{}]", accessToken);
		
		
		LOG.info("Login executed");
		return true;
	}
	
	
	public boolean sendNextStage(Long item, String filename) {
		String service = "/item/nextstage";
		JSONObject obj = new JSONObject();
		obj.append("id", item);
		
		ClientResponse response = put(service, obj);
		
		boolean success = true;
		
		if ( response.getStatus() != ClientResponse.Status.OK.getStatusCode() )
			success = false;
		
		if ( !postSignature(filename, item, null))
			success = false;
		
		deleteFile(filename);
		
		return success;
	}
	
	public boolean sendNextStage(Long[] list, String filename){
		String service = "/items/nextstage";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		obj.append("list", array);
		
		ClientResponse response = put(service, obj);
		
		boolean success = true;
		
		if ( response.getStatus() != ClientResponse.Status.OK.getStatusCode() )
			success = false;
		
		if ( !postSignature(filename, array, null))
			success = false;
		
		deleteFile(filename);
		
		return success;
	}
	
	public boolean sendStage(Long item, String stage, String filename){
		String service = "/item/sendstage";
		JSONObject obj = new JSONObject();
		obj.append("id", item);
		obj.append("stage", stage);
		
		boolean success = true;
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() != ClientResponse.Status.OK.getStatusCode() )
			success = false;
		
		
		if ( !postSignature(filename, item, stage) )
			success = false;
		
		deleteFile(filename);
		
		return success;
	}
	
	public boolean sendStage(Long[] list, String stage, String filename){
		String service = "/items/sendstage";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		obj.append("list", array);
		obj.append("stage", stage);
		
		boolean success = true;
		
		ClientResponse response = put(service, obj);
		
		if ( response.getStatus() != ClientResponse.Status.OK.getStatusCode() )
			success = false;
		
		if ( !postSignature(filename, array, stage))
			success = false;
		
		deleteFile(filename);
		
		return success;
	}
	
	public JSONObject getItem(Long item){
		String service = "/item/";
		ClientResponse response = get(service, item);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return response.getEntity(JSONObject.class);
		}
		
		return null;
	}
		
	public JSONObject getItems(Long[] list){
		String service = "/item/";
		JSONObject obj = new JSONObject();
		JSONArray array = new JSONArray(list);
		
		obj.append("list", array);
		
		ClientResponse response = post(service, obj);
		
		if ( response.getStatus() == ClientResponse.Status.OK.getStatusCode() ){
			return response.getEntity(JSONObject.class);
		}
		
		return null;
	}
	
	private boolean postSignature(String filename, long item, String stage){
		File file = new File(filename);
		
		FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		FileDataBodyPart fdp = new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		formDataMultiPart.bodyPart(fdp);
		formDataMultiPart.field("item", String.valueOf(item));
		formDataMultiPart.field("stage", stage);
		
		Client client = new Client();
		ClientResponse response = client
				.resource(address + "/signature")
				.header("Authorization", String.format("Bearer %s", accessToken))
				.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.post(ClientResponse.class, formDataMultiPart);
		
		if(response.getStatus() == ClientResponse.Status.OK.getStatusCode())
			return true;
		else
			return false;
	}
	
	private boolean postSignature(String filename, JSONArray items, String stage){
		File file = new File(filename);
		
		FormDataMultiPart formDataMultiPart = new FormDataMultiPart();
		FileDataBodyPart fdp = new FileDataBodyPart("file", file, MediaType.APPLICATION_OCTET_STREAM_TYPE);
		formDataMultiPart.bodyPart(fdp);
		formDataMultiPart.field("items", items, MediaType.APPLICATION_JSON_TYPE);
		formDataMultiPart.field("stage", stage);
		
		Client client = new Client();
		ClientResponse response = client
				.resource(address + "/signature/multi")
				.header("Authorization", String.format("Bearer %s", accessToken))
				.type(MediaType.MULTIPART_FORM_DATA_TYPE)
				.post(ClientResponse.class, formDataMultiPart);
		
		if(response.getStatus() == ClientResponse.Status.OK.getStatusCode())
			return true;
		else
			return false;
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
	 * Deletes the given file.
	 * 
	 * @param filename - absolute filename
	 */
	private void deleteFile(String filename){
		File file = new File(filename);
		file.delete();
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
