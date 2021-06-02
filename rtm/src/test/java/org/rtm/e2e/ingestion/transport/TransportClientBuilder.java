package org.rtm.e2e.ingestion.transport;

import org.rtm.commons.TransportClient;
import org.rtm.commons.utils.MeasurementUtils;

public class TransportClientBuilder {
	
	public static TransportClient buildHttpClient(String hostname, int port, MeasurementUtils measurementUtils){
		return new HttpClient(hostname, port, measurementUtils);
	}
	
	public static TransportClient buildAccessorClient(String hostname, int port){
		return null;// MeasurementAccessor.getInstance();
	}
	
}
