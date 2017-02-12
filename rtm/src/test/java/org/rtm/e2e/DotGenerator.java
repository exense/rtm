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
package org.rtm.e2e;

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.bson.Document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.MongoClient;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;


public class DotGenerator extends Thread{

	public static void main (String[] args){

		removeTestData();

		int nb_threads = 1;

		for (int i = 0; i < nb_threads; i++)
		{/**/
			//For presentation purposes
			try {
				Thread.sleep(i * 60000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/**/
			new DotGenerator().start();
		}

	}

	@SuppressWarnings("deprecation")
	public void run(){

		String serviceUrl = "http://localhost:8080/rtm/rest/ingest/generic";
		CloseableHttpClient httpclient = HttpClients.createDefault();	
		ObjectMapper mapper = new ObjectMapper();

		try{

			int dur;
			int txRootFactor = 5;

			String[] userId = {"Peter", "Michael", "Lisa", "Ted"};
			String clientIp = "10.100.1.";
			String dataSetName = "PERF_TEST";
			String transactionName = "MyMeasurement_Y";
			dur = 50;

			for (int i = 10; i < 100000; i++){
				Date dateObj = new Date();

				Map<String,String> optional = new TreeMap<String,String>();
				optional.put("client", clientIp + (int)Math.round(Math.random()*100 % 10));
				optional.put("userId", userId[(int)Math.round(Math.random()*100 % (userId.length-1))]);
				optional.put("threadId", String.valueOf(Thread.currentThread().getId()));

				Random randomGenerator = new Random();
				dur = randomGenerator.nextInt(100);

				int transactionFactor = (randomGenerator.nextInt(txRootFactor)+1);
				transactionName = "MyMeasurement_" + transactionFactor;

				dur *= transactionFactor;

				Map<String,Object> m = new HashMap<String,Object>();
				m.put("eId", dataSetName);
				m.put("name", transactionName);
				m.put("begin", dateObj.getTime());
				m.put("value", new Long(dur) > 0 ? new Long(dur) : 1);
				m.putAll(optional);

				String serialized = mapper.writeValueAsString(m);
				System.out.println("Posting measurement: " + serialized);

				HttpGet httpget = new HttpGet(serviceUrl + "?measurement="+ URLEncoder.encode(serialized));

				System.out.println("Executing request " + httpget.getRequestLine());

				ResponseHandler<String> responseHandler = new ResponseHandler<String>() {

					@Override
					public String handleResponse(final HttpResponse response) throws ClientProtocolException, IOException {
						int status = response.getStatusLine().getStatusCode();
						if (status >= 200 && status < 300) {
							HttpEntity entity = response.getEntity();
							return entity != null ? EntityUtils.toString(entity) : null;
						} else {
							throw new ClientProtocolException("Unexpected response status: " + status);
						}
					}

				};
				String responseBody = httpclient.execute(httpget, responseHandler);
				System.out.println("----------------------------------------");
				System.out.println(responseBody);
				Thread.sleep(1000);
			}
		}catch(Exception e){
			e.printStackTrace();
		}finally{
			try {
				httpclient.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}


	@SuppressWarnings("rawtypes")
	private static void removeTestData() {
		MongoClient mongoClient = new MongoClient("localhost");
		MongoDatabase db = mongoClient.getDatabase("rtm");
		MongoCollection<Document> coll = db.getCollection("measurements", Document.class);
		coll.deleteOne(new BasicDBObject("eId", "PERF_TEST"));
		mongoClient.close();
	}

}