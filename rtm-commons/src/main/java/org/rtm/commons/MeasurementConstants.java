package org.rtm.commons;

public abstract class MeasurementConstants {
	
/** Keys should be externally configured and loaded from properties file **/
	
	public static final String EID_KEY = Configuration.getInstance().getProperty("model.key.eId");
	public static final String BEGIN_KEY = Configuration.getInstance().getProperty("model.key.begin");
	public static final String VALUE_KEY = Configuration.getInstance().getProperty("model.key.value");
	public static final String NAME_KEY = Configuration.getInstance().getProperty("model.key.name");
	
	//Used in Aggregates only (could actually go back to End + Duration for more consistency)
	public static final String SESSION_KEY = Configuration.getInstance().getProperty("model.key.sId");
	public static final String END_KEY = Configuration.getInstance().getProperty("model.key.end");
	
}
