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

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.Random;
import java.util.TreeMap;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.GetMethod;
import org.rtm.commons.Measurement;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.BasicDBObject;
import com.mongodb.DB;
import com.mongodb.DBCollection;
import com.mongodb.MongoClient;


public class DotGenerator extends Thread{

	public static void main (String[] args){
			
		dropCollection();
		
		int nb_threads = 1;
		
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
				new DotGenerator().start();
			}
			
		}

	public void run(){
		try{
			
			//http://localhost:8099/rtm/rest/measurement/save/queryparam/string?measurement={%22t%22:{%22client%22:%2210.100.1.7%22,%22eId%22:%22PERF_TEST%22,%22name%22:%22MyMeasurement_1%22,%22threadId%22:%2213%22,%22userId%22:%22Lisa%22},%22n%22:{%22begin%22:1484127847377,%22value%22:37}}
			String serviceUrl = "http://localhost:8099/rtm/rest/measurement/save/queryparam/string";
			
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy,M,d,H,m,s,S");

			HttpClient httpclient = new HttpClient();
			ObjectMapper mapper = new ObjectMapper();

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
				
				String serialized = mapper.writeValueAsString(t);
				System.out.println("Posting measurement: " + serialized);
				
				GetMethod method = new GetMethod(serviceUrl + "?measurement="+ URLEncoder.encode(serialized));
				//GetMethod method = new GetMethod(serviceUrl + "?measurement="+ serialized);
				
				httpclient.executeMethod(method);
				byte[] responseBody = method.getResponseBody();
			
				 System.out.println(new String(responseBody,Charset.forName("UTF-8")));
				
				
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