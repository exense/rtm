package org.rtm.commons;

import java.io.InputStream;
import java.util.Properties;

import org.rtm.exception.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Configuration{

	/*Config file keys*/
	public static final String DSHOST_KEY = "ds.host";
	public static final String DSNAME_KEY = "ds.dbname";
	public static final String MEASUREMENTSCOLL_KEY = "ds.measurements.collectionName";
	public static final String DEBUG_KEY = "server.debug";
	
	/*Serialization prefixes*/
	public static final String NUM_PREFIX = "n";
	public static final String TEXT_PREFIX = "t";
	public static final String SPLITTER = ".";

	public static final String GRANULARITY_KEY = "granularity";
	public static final String GROUPBY_KEY = "groupby";

	private static final Logger logger = LoggerFactory.getLogger(Configuration.class);

	private static final String CONFIG_FILENAME = "rtm.properties";

	private static Configuration INSTANCE = new Configuration();
	private static Configuration NEW_INSTANCE;

	private static Boolean chooseMeForReferenceUpdate = true;
	private static Boolean chooseMeForReloadTrigger = true;

	private static boolean reloadTriggered = false;

	private Properties properties = new Properties();

	private Configuration() {
		super();

		String configFile = CONFIG_FILENAME;
		try {
			InputStream instream = this.getClass().getClassLoader().getResourceAsStream(configFile);
			properties.load(instream);
		} catch (Exception e) {
			String msg = "Could not read configuration file from the classpath: " + configFile;
			logger.error(msg, e);
			throw new IllegalStateException(msg, e);
		}
	}

	public static void triggerReload() throws ConfigurationException {

		boolean wasIChosen = false;

		synchronized(chooseMeForReloadTrigger){
			//System.out.println(Thread.currentThread().getId() + ": I was chosen for reload trigger !");
			if(chooseMeForReloadTrigger == true){
				chooseMeForReloadTrigger = false;
				wasIChosen = true;
			}
		}
		if(wasIChosen){
			//System.out.println("I'm triggering a reload !");
			NEW_INSTANCE = new Configuration();
			reloadTriggered = true;
			
			synchronized(chooseMeForReloadTrigger){	
				chooseMeForReloadTrigger = true;
			}
		}else{
			throw new ConfigurationException("Reload failed : the configuration is already currently being reloaded.");
		}
	}

	public static Configuration getInstance() {
		if(reloadTriggered)
		{
			boolean wasIChosen = false;

			synchronized(chooseMeForReferenceUpdate){
				//System.out.println(Thread.currentThread().getId() + ": I was chosen for reference update !");
				if(chooseMeForReferenceUpdate == true){
					chooseMeForReferenceUpdate = false;
					wasIChosen = true;
				}
			}

			if(wasIChosen){
				synchronized(Thread.currentThread().getClass())
				{
					//System.out.println(Thread.currentThread().getId() + ": I'm updating the reference of INSTANCE !");
					INSTANCE = NEW_INSTANCE;
					NEW_INSTANCE = null;
					reloadTriggered = false;
					chooseMeForReferenceUpdate = true;
				}
			}
		}
		return INSTANCE;
	}

	public String getProperty(String name) {
		return properties.getProperty(name);
	}

	public Integer getPropertyAsInteger(String name) {
		String prop = properties.getProperty(name);
		if(prop!=null) {
			return Integer.parseInt(prop);
		} else {
			return null;
		}
	}

	public boolean getPropertyAsBoolean(String name) {
		String prop = properties.getProperty(name);
		if(prop!=null) {
			return Boolean.parseBoolean(prop);
		} else {
			return false;
		}
	}

	public Properties getUnderlyingPropertyObject() {
		return properties;
	}

}
