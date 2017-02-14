package org.rtm.e2e.ingestion;

import java.util.Map;

import org.rtm.e2e.ingestion.transport.TransportClient;

public class E2EIngestionSimulator {
	
	public static String sendStructuredMeasurement(TransportClient tc, Map<String, Object> m) throws TransportException{
		String result = tc.sendStructuredMeasurement(m);
		
		if(!tc.isSuccessful(result))
			throw new TransportException("Measurement send failed for measurement: " + m + ", and using TransportClient: "+tc +". Result was:" + result);
		
		return result;
	}

	public static void sendGenericMeasurement(){
		
	}
	
}
