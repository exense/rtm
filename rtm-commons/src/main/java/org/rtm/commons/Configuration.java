package org.rtm.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration{

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private static final String CONFIG_FILENAME = "rtm.properties";

	private static Configuration INSTANCE;
	private Properties properties = new Properties();

	@Deprecated
	private Configuration() {
		super();

		String configFile = CONFIG_FILENAME;
		try {
			InputStream instream = this.getClass().getClassLoader().getResourceAsStream(configFile);
			properties.load(instream);
			INSTANCE = this;
		} catch (Exception e) {
			String msg = "Could not read configuration file from the classpath: " + configFile;
			logger.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	private Configuration(File f) {
		super();

		String configFile = CONFIG_FILENAME;
		try {
			InputStream instream = new FileInputStream(f);
			properties.load(instream);
			INSTANCE = this;
		} catch (Exception e) {
			String msg = "Could not read configuration file from the classpath: " + configFile;
			logger.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	public static void initSingleton(File f){
		INSTANCE = new Configuration(f);
	}

	public static Configuration getInstance() {
		// For compatibility with war (no explicit properties linkage)
		if(INSTANCE == null){ // we can take the risk of initializing twice, doesn't matter
			synchronized(Configuration.class){
				INSTANCE = new Configuration();
			}
		}

		return INSTANCE;
	}

	public String getProperty(String name) throws Exception {
		String prop = properties.getProperty(name);
		if(prop!=null) {
			return prop;
		} else {
			throw new Exception("Configuration issue - property not found: " + name);
		}
	}

	public Integer getPropertyAsInteger(String name) throws Exception {
		String prop = properties.getProperty(name);
		if(prop!=null) {
			return Integer.parseInt(prop);
		} else {
			throw new Exception("Configuration issue - property not found: " + name);
		}
	}

	public boolean getPropertyAsBoolean(String name) throws Exception {
		String prop = properties.getProperty(name);
		if(prop!=null) {
			return Boolean.parseBoolean(prop);
		} else {
			throw new Exception("Configuration issue - property not found: " + name);
		}
	}

	public Properties getUnderlyingPropertyObject() {
		return properties;
	}

}
