package org.rtm.commons;

import java.util.Map;

public interface TransportClient {
	
	public void sendStructuredMeasurement(Map<String, Object> measurement) throws TransportException;
	public void sendStructuredMeasurement(String json) throws TransportException;
	
	//public void sendGenericMeasurement(Map<String, Object> measurement);
	//public void sendGenericMeasurement(String json);
	
	public void close();
	
	public String toString();

}
