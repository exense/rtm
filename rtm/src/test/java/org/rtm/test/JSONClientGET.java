package org.rtm.test;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.rtm.rest.SimpleResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
 
public class JSONClientGET{
 
  public static void main(String[] args) {
	try {
 
		ClientConfig clientConfig = new DefaultClientConfig();
		 ObjectMapper mapper = new ObjectMapper();
		 JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
		 jacksonProvider.setMapper(mapper);
		 clientConfig.getSingletons().add(jacksonProvider); 
		 Client client = Client.create(clientConfig); 
 
		
		//String input = "{\"begin\":1403707612931,\"duration\":132,\"data\":{\"eId\":\"myeid\",\"name\":\"myname\"}}";
		
				
		//WebResource webResource = client.resource("http://localhost:8080/rtm/rest/transaction/save/default");
		//WebResource webResource = client.resource("http://localhost:8080/rtm/rest/transaction/save/queryparam/string");
		 WebResource webResource = client.resource("http://localhost:8080/rtm/rest/service/allmeasurements");
		
		//ClientResponse response = webResource.queryParam("transaction", input).accept("application/json").get(ClientResponse.class);
		 ClientResponse response = webResource.accept("application/json").get(ClientResponse.class);
		
		System.out.println(response + "\n" + response.getEntity(SimpleResponse.class));
		

 
	  } catch (Exception e) {
 
		e.printStackTrace();
 
	  }
 
	}
}