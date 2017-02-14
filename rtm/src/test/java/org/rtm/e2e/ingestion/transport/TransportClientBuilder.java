package org.rtm.e2e.ingestion.transport;

public class TransportClientBuilder {
	
	public static TransportClient buildHttpClient(String hostname, int port){
		return new HttpClient(hostname, port);
	}
	
	public static TransportClient buildAccessorClient(String hostname, int port){
		//TODO : via accessor
		return null;
	}
	
}
