package com.nehp.rfid_system.reader.server_connection;

import javax.swing.SwingWorker;

/**
 * This class provides the necessary methods to send and receive data to/from the server.
 * @author Trav
 *
 */
public class Server {
	
	private String address = null;
	private String user = null;
	private String password = null;
	
	private String auth_header = null;
	
	public Server(String address, String user, String password){
		this.address = address;
		this.user = user;
		this.password = password;
	}
	
	class Login extends SwingWorker<Void, Void>{

		@Override
		protected Void doInBackground() throws Exception {
			
			return null;
		}
		
	}
	
	public boolean login(){
		Login login = new Login();
		login.execute();
		
		return true;
	}
}
