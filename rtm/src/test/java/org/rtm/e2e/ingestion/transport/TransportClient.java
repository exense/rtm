package org.rtm.e2e.ingestion.transport;

import java.util.Map;

public interface TransportClient {
	
	public String sendStructuredMeasurement(Map<String, Object> measurement);
	public String sendStructuredMeasurement(String json);
	
	public String sendGenericMeasurement(Map<String, Object> measurement);
	public String sendGenericMeasurement(String json);
	
	public boolean isSuccessful(String response);
	
	public void close();
	
	public String toString();

}
