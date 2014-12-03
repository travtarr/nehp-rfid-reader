package com.nehp.rfid_system.reader;

import java.io.IOException;

import org.eclipse.swt.widgets.Display;

import com.nehp.rfid_system.reader.helpers.Config;
import com.nehp.rfid_system.reader.server_connection.Server;
import com.nehp.rfid_system.reader.windows.Loading;
import com.nehp.rfid_system.reader.windows.MainWindow;

public class Launcher {
		
	/**
	 * Launch the application.
	 * @param args
	 */
	public Launcher(){
		Display display = Display.getDefault();
		Config config = null;
		try {
			config = new Config();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Server server = new Server(config.getAddress(), config.getUser(), config.getPassword());
		
		// Attempt to login
		if(!server.login()){
			System.out.println("Unable to login");
		}
			
		MainWindow mainWindow = new MainWindow(display, server);
		//Loading loading = new Loading(display);
		//loading.open();
		mainWindow.open();
		
	}
	
	public static void main(String[] args) {
		new Launcher();
	}
	
}
