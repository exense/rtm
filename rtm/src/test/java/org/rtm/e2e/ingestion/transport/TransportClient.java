package org.rtm.e2e.ingestion.transport;

import java.util.Map;

import org.rtm.e2e.ingestion.TransportException;

public interface TransportClient {
	
	public String sendStructuredMeasurement(Map<String, Object> measurement) throws TransportException;
	public String sendStructuredMeasurement(String json) throws TransportException;
	
	public String sendGenericMeasurement(Map<String, Object> measurement);
	public String sendGenericMeasurement(String json);
	
	public boolean isSuccessful(String response);
	
	public void close();
	
	public String toString();

}
