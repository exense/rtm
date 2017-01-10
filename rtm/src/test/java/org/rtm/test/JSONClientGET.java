/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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