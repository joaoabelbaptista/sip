/**
 * Salesforce Integration Platform - SIP.
 * 
 * Subject: Configurations read from properties.
 * History: aosantos, 2016-07-06, Initial Release.
 * 
 * 
 */
package com.vrs.sip;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class Configuration extends Properties {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	
	public static final String PROPERTIES_FILENAME = "sip.properties";
	
	public static Configuration configurationInstance;

	public static Boolean isInstall = false;
	
	public Configuration() throws FileNotFoundException, IOException {
		InputStream is;
		
		is = ClassLoader.getSystemResourceAsStream(PROPERTIES_FILENAME);
		
		if (is == null) {
			throw new RuntimeException("Error Loading " + PROPERTIES_FILENAME);
		}
		
		load(is);
	}
	
	public class Log {
		public String directory;
		public String level;
		public Boolean pipeline;
		public Boolean connection;
		public Boolean isDatabaseLog;
	}
	
	public class Scripts {
		public String home;
		public String exec;
	}
	
	public class Server {
		public Boolean audit;
		public Integer poolingSeconds;
		public String metadataBaseDirectory;
		public String tmpDirectory;
		public String modelDirectory;
		public Boolean stdoutDebug;
	}
	
	public static Configuration getInstance() throws FileNotFoundException, IOException {
		if (configurationInstance == null) {
			configurationInstance = new Configuration();
		}
		
		return configurationInstance;
	}
		
	public Log getLog() {
		Log log = new Log();
		
		log.directory = getString("log.directory");
		log.level = getString("log.level");
		log.pipeline = getBoolean("log.pipeline");
		log.connection = getBoolean("log.connection");
		log.isDatabaseLog = getBoolean("log.database");
		
		if (isInstall == true) {
			log.isDatabaseLog = false;
		}
		
		return log;
	}
	
	public Scripts getScripts() {
		Scripts scripts = new Scripts();
		
		scripts.home = getString("scripts.home");
		scripts.exec = getString("scripts.exec");
		
		return scripts;
	}
	
	public Server getServer() {
		Server server = new Server();
		
		server.audit = getBoolean("server.audit");
		server.poolingSeconds = getInteger("server.poolingseconds");
		server.metadataBaseDirectory = getString("server.metadataBaseDirectory");
		server.tmpDirectory = getString("server.tmpDirectory");
		server.modelDirectory = getString("server.modelDirectory");
		server.stdoutDebug = getBoolean("server.stdoutDebug");
		
		return server;
	}
	
	public String getString(String name) {
		return getProperty(name);
	}
	public Boolean getBoolean(String name) {
		return Boolean.valueOf(getProperty(name));
	}
	
	public Integer getInteger(String name) {		
		return Integer.valueOf(getProperty(name));
	}
}
