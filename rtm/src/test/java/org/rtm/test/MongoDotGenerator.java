package org.rtm.test;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import javax.ws.rs.core.MediaType;

import org.apache.commons.httpclient.HttpClient;
import org.codehaus.jackson.jaxrs.JacksonJaxbJsonProvider;
import org.codehaus.jackson.map.ObjectMapper;
import org.rtm.commons.Measurement;
import org.rtm.rest.SimpleResponse;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class MongoDotGenerator extends Thread{

	public static void main (String[] args){
			
			for (int i = 0; i < 10; i++)
			{
				new MongoDotGenerator().start();
			}
			
		}
		
	public void run(){
		try{
			
//			DB db = new MongoClient().getDB("perftest");
//			Jongo jongo = new Jongo(db);
//			MongoCollection transactions = jongo.getCollection("transactions");
//			
			
			ClientConfig clientConfig = new DefaultClientConfig();
			 ObjectMapper mapper = new ObjectMapper();
			 JacksonJaxbJsonProvider jacksonProvider = new JacksonJaxbJsonProvider();
			 jacksonProvider.setMapper(mapper);
			 clientConfig.getSingletons().add(jacksonProvider); 
			 Client client = Client.create(clientConfig); 

			WebResource webResource = client.resource("http://localhost:8080/rtm/rest/measurement/save/default");
			webResource.accept(MediaType.APPLICATION_JSON);
			webResource.type(MediaType.APPLICATION_JSON);
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy,M,d,H,m,s,S");

			HttpClient httpclient = new HttpClient();

			int dur;
			
			int txRootFactor = 5;

			String userId = "stepUser_X";
			String clientIp = "10.100.1.0";
			String dataSetName = "DEV_NTX" + Integer.toString(txRootFactor);
			String transactionName = "MyMeasurement_Y";
			dur = 50;
			
			//for (int i = 0; i < 1; i++){
			for (int i = 10; i < 100000; i++){
				Date dateObj = new Date();
				
				Map<String,String> optional = new TreeMap<String,String>();
				optional.put("client", clientIp);
				optional.put("userId", userId);
				

				Random randomGenerator = new Random();
				dur = randomGenerator.nextInt(100);
				//faster
				//dur = randomGenerator.nextInt(5);
	
				int transactionFactor = (randomGenerator.nextInt(txRootFactor)+1);
				transactionName = "MyMeasurement_" + transactionFactor;
				
				dur *= transactionFactor;
				//System.out.println(dur);
				
				Measurement t = new Measurement();
				t.setTextAttribute("eId", dataSetName);
				t.setTextAttribute("name", transactionName);
				t.setNumericalAttribute("begin", dateObj.getTime());
				t.setNumericalAttribute("value", new Long(dur));
				t.setTextAttributes(optional);
				/*
				WriteResult wr = MeasurementAccessor.getInstance().saveMeasurement(t);
				*/
				
				ClientResponse response = webResource.type("application/json")
						.post(ClientResponse.class, t);

				SimpleResponse output = response.getEntity(SimpleResponse.class);
				System.out.println(output);
				
				
				
				//System.out.println(wr);
				
				
				//String body = "{\"_id\": \"perfdoc_"+dataSetName+"_"+i+"\",\"dataSet\": \""+ dataSetName+"\",\"transactionName\": \""+transactionName+"\",\"userId\": \""+userId+"\",\"client\": \""+clientIp+"\",\"date\": "+dateObj.getTime()+",\"duration\": "+dur+",\"year\": "+y+",\"month\": "+m+",\"day\": "+d+",\"hour\": "+h+",\"minute\": "+min+",\"second\": "+s+",\"ms\": "+ms+"}";
				
				Thread.sleep(dur);
			}
		}catch(Exception e){e.printStackTrace();}
	}
}