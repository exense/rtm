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

import java.net.UnknownHostException;
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
import org.w3c.dom.Document;

import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.DBObject;
import com.mongodb.MongoClient;
import com.mongodb.util.JSON;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;


public class MongoDotGenerator extends Thread{

	public static void main (String[] args){
			
		dropCollection();
		
		int nb_threads = 5;
		
			for (int i = 0; i < nb_threads; i++)
			{/**/
				//For presentation purposes
				try {
					Thread.sleep(i * 60000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				/**/
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

			String[] userId = {"Peter", "Michael", "Lisa", "Ted"};
			String clientIp = "10.100.1.";
			String dataSetName = "PERF_TEST";
			String transactionName = "MyMeasurement_Y";
			dur = 50;
			
			//for (int i = 0; i < 1; i++){
			for (int i = 10; i < 100000; i++){
				Date dateObj = new Date();
				
				Map<String,String> optional = new TreeMap<String,String>();
				optional.put("client", clientIp + (int)Math.round(Math.random()*100 % 10));
				optional.put("userId", userId[(int)Math.round(Math.random()*100 % (userId.length-1))]);
				optional.put("threadId", String.valueOf(Thread.currentThread().getId()));

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
				t.setNumericalAttribute("value", new Long(dur) > 0 ? new Long(dur) : 1);
				t.setTextAttributes(optional);
				/*
				WriteResult wr = MeasurementAccessor.getInstance().saveMeasurement(t);
				*/
				
				System.out.println("Posting measurement: " + t);
				
				ClientResponse response = webResource.type("application/json")
						.post(ClientResponse.class, t);

				SimpleResponse output = response.getEntity(SimpleResponse.class);
				System.out.println(output);
				
				
				
				//System.out.println(wr);
				
				
				//String body = "{\"_id\": \"perfdoc_"+dataSetName+"_"+i+"\",\"dataSet\": \""+ dataSetName+"\",\"transactionName\": \""+transactionName+"\",\"userId\": \""+userId+"\",\"client\": \""+clientIp+"\",\"date\": "+dateObj.getTime()+",\"duration\": "+dur+",\"year\": "+y+",\"month\": "+m+",\"day\": "+d+",\"hour\": "+h+",\"minute\": "+min+",\"second\": "+s+",\"ms\": "+ms+"}";
				
				Thread.sleep(1000);
			}
		}catch(Exception e){e.printStackTrace();}
	}
	
	
private static void dropCollection() {
	MongoClient c = new MongoClient("localhost");
	DB db = c.getDB("rtm");
	DBCollection coll = db.getCollection("measurements");
	coll.remove(new BasicDBObject("t.eId", "PERF_TEST1"));
	c.close();
}

}