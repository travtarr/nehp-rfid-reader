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

import com.nehp.rfid_system.reader.windows.MainWindow;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.core.util.MultivaluedMapImpl;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.MultivaluedMap;

/**
 * This class provides the necessary methods to send and receive data to/from the server.
 * @author Trav
 *
 */
public class Server {
	
	private final String GRANT_TYPE = "password";
	
	private String address = null;
	private String user = null;
	private String password = null;
	
	private String access_token = null;
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
				System.out.println("Bad response: " + response.getStatus());
				return false;
			}
			
			System.out.println("Good response");
			
			JSONObject obj = new JSONObject(response.getEntity(String.class));
			JSONArray array = obj.getJSONArray("api_key");
			
			progressBar.setSelection(75);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
			
			access_token = array.getJSONObject(0).getJSONObject("access_token").getString("string");

			progressBar.setSelection(100);
			try { Thread.sleep(100); } catch (InterruptedException e) {}
						
			System.out.println("Connected with token: [" + access_token + "]");
		}
	
		progressBar.setVisible(false);
		
		System.out.println("Login executed");
		return true;
	}
		
	
	/**
	 * Determine if server is connected.
	 * @return
	 */
	public boolean isConnected(){
		if(access_token != null && hasInternetConnection())
			return true;
		else
			return false;
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
			System.out.println("unknown host");
		} catch (IOException e) {
			// do nothing - boolean defaulted false
			System.out.println("IOexception");
		} 
		return connected;
	}
}
