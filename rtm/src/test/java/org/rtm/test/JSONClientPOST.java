package org.rtm.test;

import java.util.Date;

import javax.ws.rs.core.MediaType;

import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.rtm.commons.Measurement;
import org.rtm.dao.NumericalFilter;
import org.rtm.dao.Selector;
import org.rtm.dao.TextFilter;
import org.rtm.rest.SimpleResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.client.filter.LoggingFilter;

public class JSONClientPOST {

	public static void main(String[] args) {
		postGetMeasurements();
	}
	
	public static void postGetMeasurements(){
		
		Selector slt = new Selector();
		TextFilter f1 = new TextFilter();
		f1.setKey("name");
		f1.setValue("MyMeasurement_5");

		TextFilter f2 = new TextFilter();
		f2.setKey("status");
		f2.setValue("PASSED");

		slt.addTextFilter(f1);
		//slt.addTextFilter(f2);
		
		/***/
		
		NumericalFilter nf1 = new NumericalFilter();
		nf1.setKey("begin");
		nf1.setMinValue(new Date().getTime());
		nf1.setMaxValue(new Date().getTime());

		NumericalFilter nf2 = new NumericalFilter();
		nf2.setKey("value");
		nf2.setMinValue(200L);
		nf2.setMaxValue(500L);

		//slt.addNumericalFilter(nf1);
		slt.addNumericalFilter(nf2);

		ClientConfig clientConfig = new DefaultClientConfig();
		
//		ClientConfig config = new DefaultClientConfig();
//		Client client = new Client(new URLConnectionClientHandler(
//		        new HttpURLConnectionFactory() {
//		    Proxy p = null;
//		    @Override
//		    public HttpURLConnection getHttpURLConnection(URL url)
//		            throws IOException {
//		        if (p == null) {
//		            if (System.getProperties().containsKey("http.proxyHost")) {
//		                p = new Proxy(Proxy.Type.HTTP,
//		                        new InetSocketAddress(
//		                        System.getProperty("http.proxyHost"),
//		                        Integer.getInteger("http.proxyPort", 80)));
//		            } else {
//		                p = Proxy.NO_PROXY;
//		            }
//		        }
//		        return (HttpURLConnection) url.openConnection(p);
//		    }
//		}), config);

		
		ObjectMapper mapper = new ObjectMapper();
		 JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
		 jacksonProvider.setMapper(mapper);
		 clientConfig.getSingletons().add(jacksonProvider); 
		 Client client = Client.create(clientConfig);
		 client.addFilter(new LoggingFilter(System.out));
		
		WebResource webResource = client.resource("http://localhost:8080/rtm/rest/service/measurement");
		webResource.accept(MediaType.APPLICATION_JSON);
		webResource.type(MediaType.APPLICATION_JSON);
		
		ClientResponse response = webResource.type("application/json")
				.post(ClientResponse.class, slt);

		String output = response.getEntity(String.class);
		//System.out.println(output);
	}
	
	public static void postInsert(){
		try {

			ClientConfig clientConfig = new DefaultClientConfig();
			 ObjectMapper mapper = new ObjectMapper();
			 JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
			 jacksonProvider.setMapper(mapper);
			 clientConfig.getSingletons().add(jacksonProvider); 
			 Client client = Client.create(clientConfig); 
			
			WebResource webResource = client.resource("http://localhost:8080/rtm/rest/service/measurement");
			webResource.accept(MediaType.APPLICATION_XML);
			webResource.type(MediaType.APPLICATION_XML);

			Measurement input = new Measurement();
			
			input.setNumericalAttribute("date", new Date().getTime());
			input.setNumericalAttribute("duration", 123L);
			input.setTextAttribute("eId", "myeid");
			input.setTextAttribute("name","toto");
			
			ClientResponse response = webResource.type("application/json")
					.post(ClientResponse.class, input);

			SimpleResponse output = response.getEntity(SimpleResponse.class);
			System.out.println(output);

		} catch (Exception e) {

			e.printStackTrace();

		}
	}
	
}