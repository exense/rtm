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

import java.io.DataInputStream;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.rtm.commons.MeasurementAccessor;

@SuppressWarnings("deprecation")
public class URLPollerHttpClient {

	public static boolean debug = false;

	public static void main(String... arg){


		// URL - sleepTime - transactionName - groupId
		if( (arg.length != 4) || (arg[0] == null || arg[0].trim().isEmpty() || arg[1] == null || arg[1].trim().isEmpty() || arg[2] == null || arg[2].trim().isEmpty() || arg[3] == null || arg[3].trim().isEmpty()))
		{
			//System.out.println("ERROR : Incorrect number of arguments");
			return;
		}

		String url = arg[0];
		long sleepTime = Long.parseLong(arg[1]);
		String name = arg[2];
		String groupId = arg[3];

		MeasurementAccessor rtmAccessor = MeasurementAccessor.getInstance();

		System.out.println("Starting endless loop with the following arguments :");
		System.out.println("url="+ url);
		System.out.println("sleepTime="+ sleepTime);
		System.out.println("name="+ name);
		System.out.println("groupId="+ groupId);

		long[] values;
		long call = 0;
		long response = 0;

		while(true){
			long begin = System.currentTimeMillis();
			values = makeHttpCall(url);

			call=values[0];
			response=values[1];

			Map<String,Object> m = new HashMap<String,Object>();
			m.put("url", url);
			m.put("name", name);
			m.put("eId", groupId);
			m.put("value", call+response);
			m.put("begin", begin);
			m.put("call", call);
			m.put("response", response);
			m.put("sleepTime", sleepTime);
			rtmAccessor.saveMeasurement(m);

			try {
				Thread.sleep(sleepTime);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

	}

	private static long[] makeHttpCall(String url) {

		long[] values = new long[2];

		HttpParams httpParameters = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParameters, 10000);
		int timeoutSocket = 35000;
		HttpConnectionParams.setSoTimeout(httpParameters, timeoutSocket);
		DefaultHttpClient httpclient = new DefaultHttpClient(httpParameters);

		HttpGet httpget = new HttpGet(url);
		httpget.setHeader("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8");
		httpget.setHeader("Accept-Encoding","gzip, deflate, sdch");
		httpget.setHeader("Accept-Language","fr-FR,fr;q=0.8,en-US;q=0.6,en;q=0.4,de;q=0.2");
		httpget.setHeader("Cache-Control","no-cache");
		httpget.setHeader("Connection","keep-alive");
		httpget.setHeader("Host",url.split("/")[2]);
		httpget.setHeader("Pragma","no-cache");
		httpget.setHeader("User-Agent","Mozilla/5.0 (Windows NT 10.0; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/49.0.2623.112 Safari/537.36");


		long start = 0;
		long end = 0;

		try {

			start = System.currentTimeMillis();
			HttpResponse response = httpclient.execute(httpget);// calling server
			end = System.currentTimeMillis();
			values[0] = (end - start);

			start = System.currentTimeMillis();
			HttpEntity r_entity = response.getEntity();  //get response

			if(debug){
				System.out.println("Reponse Header : Begin...");          // response headers
				System.out.println("Reponse Header : StatusLine:"+response.getStatusLine());
			}
			if(!response.getStatusLine().toString().contains("200 OK"))
				throw new Exception("Http Error:" + response.getStatusLine().toString());
			Header[] headers = response.getAllHeaders();
			for(Header h:headers){
				if(debug)
					System.out.println("Reponse Header :"+h.getName() + ": " + h.getValue());
			}
			if(debug)
				System.out.println("Reponse Header : END...");

			byte [] buf = new byte[1000];

			StringBuffer result = new StringBuffer();

			if (r_entity != null) {
				DataInputStream is = new DataInputStream(
						r_entity.getContent());
				while(is.read(buf) >= 0){
					result.append(new String(buf));
				}
			}
		} catch (Exception E) {
			System.out.println("Exception While Connecting : "+E.getMessage());
			E.printStackTrace();
		}
		end = System.currentTimeMillis();

		httpclient.getConnectionManager().shutdown(); //shut down the connection
		httpclient.close();
		values[1] = (end-start);
		return values;
	}
}
