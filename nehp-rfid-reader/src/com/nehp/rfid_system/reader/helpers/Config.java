package com.nehp.rfid_system.reader.helpers;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Config {

	private String address = null;
	private String user = null;
	private String password = null;

	public Config() throws IOException {
		Properties prop = new Properties();
		String propFileName = "config.properties";

		InputStream inputStream = getClass()
				.getResourceAsStream(propFileName);

		if (inputStream != null) {
			prop.load(inputStream);
		} else {
			throw new FileNotFoundException("property file '" + propFileName
					+ "' not found in the classpath");
		}

		// set the property values
		address = prop.getProperty("address");
		user = prop.getProperty("user");
		password = prop.getProperty("password");
	
	}

	public String getAddress() {
		return address;
	}

	public String getUser() {
		return user;
	}

	public String getPassword() {
		return password;
	}
}
