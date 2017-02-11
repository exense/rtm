package org.rtm.commons;

public abstract class MeasurementConstants {
	
/** Keys should be externally configured and loaded from properties file **/
	
	public static final String BEGIN_KEY = "begin";
	public static final String VALUE_KEY = "value";
	public static final String NAME_KEY = "name";
	
	//TODO: Used in Aggregates only (could actually go back to End + Duration for more consistency)
	public static final String SESSION_KEY = "sId";
	public static final String END_KEY = "end";
	
}
