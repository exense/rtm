package org.rtm.rest.security;

import java.util.Map;

import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.client.Invocation.Builder;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.NewCookie;

import org.glassfish.jersey.media.multipart.MultiPartFeature;

public class UnsecureAbstractClient {

	protected Client client;
	protected String serverUrl;
	protected Map<String, NewCookie> cookies;

	public UnsecureAbstractClient(String serverUrl) {
		this.serverUrl = serverUrl;
		createClient();
	}
	
	private void createClient() {
		client = ClientBuilder.newClient();
		client.register(JacksonMapperProvider.class);
		client.register(MultiPartFeature.class);
		//client.register(ObjectMapperResolver.class);
		//client.register(JacksonJsonProvider.class);
	}
	
	protected Builder requestBuilder(String path) {
		return requestBuilder(path, null);
	}
	
	protected Builder requestBuilder(String path, Map<String, String> queryParams) {
		WebTarget target = client.target(serverUrl + path);
		if(queryParams!=null) {
			for(String key:queryParams.keySet()) {
				target=target.queryParam(key, queryParams.get(key));
			}
		}
		Builder b = target.request();
		b.accept(MediaType.APPLICATION_JSON);
		if(cookies!=null) {
			for(NewCookie c:cookies.values()) {
				b.cookie(c);
			}			
		}
		return b;
	}

}