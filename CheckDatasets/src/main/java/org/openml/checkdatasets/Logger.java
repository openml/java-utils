package org.openml.checkdatasets;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;

import org.openml.checkdatasets.utils.Config;

/**
 * @author Arlind Kadra
 *
 * Simple class which will log the results/errors.
 */

public class Logger {
	
	private static Logger instance;
	private File logFile; 
	private final String DIR = System.getProperty("user.dir");
	
	private Logger() {
		
		Config config = new Config();
		// Check whether we have a preference on where to save the log file.
		if(config.getLogPath() != null) {
			logFile = new File(config.getLogPath(), "errorLog.txt");
		} else {
			logFile = new File(DIR, "errorLog.txt");
		}
		if(!(logFile.exists() && logFile.isFile())) {
			try {				
				logFile.createNewFile();
			} catch(IOException e) {
				System.out.println("Not able to log messages, problems creating the file:" + e.getMessage());
				e.printStackTrace();
			}
		}
	}
	
	public static synchronized Logger getInstance() {
		
		if(instance != null){
			return instance;
		} else {
			instance = new Logger();
			return instance;
		}
	}
	
	public void logToFile(String text) {
		
		Date date = new Date();
		try {
			FileWriter writer = new FileWriter(logFile, true);
			writer.write(date.toString() + "-" + text );
			writer.write(System.getProperty("line.separator"));
			writer.close();
		} catch(IOException e) {
			System.out.println("Not able to log messages, problems writting to the file:" + e.getMessage());
			e.printStackTrace();
		}
	}
}