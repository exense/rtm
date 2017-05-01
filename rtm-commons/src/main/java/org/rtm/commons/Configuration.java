package org.rtm.commons;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration{

	/*Config file keys*/
	public static final String DSHOST_KEY = "ds.host";
	public static final String DSNAME_KEY = "ds.dbname";
	public static final String MEASUREMENTSCOLL_KEY = "ds.measurements.collectionName";
	public static final String DEBUG_KEY = "server.debug";

	public static final String GRANULARITY_KEY = "granularity";
	public static final String GROUPBY_KEY = "groupby";

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private static final String CONFIG_FILENAME = "rtm.properties";

	private static Configuration INSTANCE;

	// use to be initialized to new Configuration()
	private static Configuration NEW_INSTANCE;
	private static boolean reloadTriggered = false;
	private Properties properties = new Properties();
	private static boolean initialized = false;

	// Deprecated : moving to jetty we're initializing the config explicitely
	@Deprecated
	private Configuration() {
		super();

		String configFile = CONFIG_FILENAME;
		try {
			InputStream instream = this.getClass().getClassLoader().getResourceAsStream(configFile);
			properties.load(instream);
			INSTANCE = this;
			initialized = true;
		} catch (Exception e) {
			String msg = "Could not read configuration file from the classpath: " + configFile;
			logger.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	// Now initializing explicitly with the properties file
	private Configuration(File f) {
		super();

		String configFile = CONFIG_FILENAME;
		try {
			InputStream instream = new FileInputStream(f);
			properties.load(instream);
			INSTANCE = this;
			initialized = true;
		} catch (Exception e) {
			String msg = "Could not read configuration file from the classpath: " + configFile;
			logger.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	// To be called explicitly prior to using getInstance() - jetty compat
	public static void initSingleton(File f){
		INSTANCE = new Configuration(f);
	}

	public static synchronized void triggerReload(){
		NEW_INSTANCE = new Configuration();
		reloadTriggered = true;
	}

	public static Configuration getInstance() {
		if(reloadTriggered)
		{
			synchronized(Configuration.class)
			{
				INSTANCE = NEW_INSTANCE;
				NEW_INSTANCE = null;
				reloadTriggered = false;
			}

		}

		// For compatibility with war (no explicit properties linkage)
		if(initialized == false || INSTANCE == null){
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
