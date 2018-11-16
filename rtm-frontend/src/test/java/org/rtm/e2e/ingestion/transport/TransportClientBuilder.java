package org.rtm.e2e.ingestion.transport;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.TransportClient;

public class TransportClientBuilder {
	
	public static TransportClient buildHttpClient(String hostname, int port){
		return new MeasurementClient(hostname, port);
	}
	
	public static TransportClient buildAccessorClient(String hostname, int port){
		return MeasurementAccessor.getInstance();
	}
	
}
