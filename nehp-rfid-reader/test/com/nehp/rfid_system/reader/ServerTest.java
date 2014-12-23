package com.nehp.rfid_system.reader;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.Assertions.assertThat;

import com.nehp.rfid_system.reader.server_connection.Server;

public class ServerTest {
	
	private final String address = "https://www.nehptracker.com/service";
	private final String user = "alpha";
	private final String password = "alpha";
	
	Server server;
	
	@Before
	public void initialize(){
		server = new Server(address, user, password);
	}
	
	@Test
	public void login(){
		assertThat( server.login() ).isTrue();
	}
	
	@Test
	public void getItem(){
		
	}
	
	@Test
	public void getItems(){
		
	}
	 
	@Test
	public void sendNextStageSingle(){
		
	}
	
	@Test
	public void sendNextStageMulti(){
		
	}
	
	@Test
	public void sendStageSingle(){
		
	}
	
	@Test
	public void sendStageMulti(){
		
	}
}
