package org.rtm.commons;

public abstract class MeasurementConstants {

	/** Keys should be externally configured and loaded from properties file **/

	public static String EID_KEY = null;
	public static String BEGIN_KEY = null;
	public static String VALUE_KEY = null;
	public static String NAME_KEY = null;

	//Used in Aggregates only (could actually go back to End + Duration for more consistency)
	public static String SESSION_KEY = null;
	public static String END_KEY = null;

	static{
		try {
			EID_KEY = Configuration.getInstance().getProperty("model.key.eId");
			BEGIN_KEY = Configuration.getInstance().getProperty("model.key.begin");
			VALUE_KEY= Configuration.getInstance().getProperty("model.key.value");
			NAME_KEY = Configuration.getInstance().getProperty("model.key.name");

			//Used in Aggregates only (could actually go back to End + Duration for more consistency)
			SESSION_KEY = Configuration.getInstance().getProperty("model.key.sId");
			END_KEY = Configuration.getInstance().getProperty("model.key.end");
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
