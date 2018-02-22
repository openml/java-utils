package org.openml.checkdatasets.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;

import org.openml.checkdatasets.Logger;

// Class use to retrieve and provide possible configuration parameters.

public class Config {

	private final String DIR = System.getProperty("user.dir");
	private HashMap<String, String> parameters;
	
	public Config() {
		
		try {
			readConfig();
		} catch(IOException e) {
			Logger.getInstance().logToFile("Error-Problem reading the config file: " + e.toString());
		}
	}
	
	/**
	 * Get the path in which we are supposed to create the log file and store information from the parameters.
	 * 
	 * @return - String containing path if it has been set, otherwise null.
	 */
	public String getLogPath() {
		
		if(parameters != null) {
			if(parameters.containsKey("log-path")) {
				return parameters.get("log-path");
			}
		} 
		return null;
	}
	
	/**
	 * Check whether there is a configuration file. 
	 * If a configuration file exists, get the parameters with their values from the file.
	 * 
	 * @throws IOException
	 */
	private void readConfig() throws IOException {
		
		// config parameters
		parameters = new HashMap<String, String>();
		File configFile = new File(DIR, "settings.ini");
		if(configFile.exists() && configFile.isFile()) {
			BufferedReader reader = new BufferedReader(new FileReader(configFile));
			String line;
			while((line = reader.readLine()) != null) {
				String[] elements = line.split("=");
				parameters.put(elements[0].trim(), elements[1].trim());
			}
			reader.close();	 
		}	
	}
}