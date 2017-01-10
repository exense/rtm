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

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.methods.PostMethod;
import org.rtm.core.MeasurementAggregator;

public class SendSLAStatistics {

	public static void main(String[] args) throws Exception{

		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");
		HttpClient httpclient = new HttpClient();
		String response = null;

		Date begin = sdf.parse("20131201000000");
		Date end = sdf.parse("20131215000000");
		String transactionName = "Falldossier_Suche";
		/*
		String metric = "pcl99";
		String exId = "P03.E03.KPI2.2";
		String granularity = "3600000"; 
		*/
		/*
		String metric = "pcl90";
		String exId = "P03.E03.KPI2.1";
		String granularity = "3600000";
		*/
		
		// pro Tag
		String metric = "pcl90";
		String exId = "P03.E03.KPI2.3";
		String granularity = "86400000"; 
		long gran = Long.parseLong(granularity);
		
		Calendar cal = Calendar.getInstance();

		Date leftB = begin;
		cal.setTime(leftB);
		MeasurementAggregator.addMillisecondsToCalWithLong(cal, gran-1);
		Date rightB = cal.getTime();

		
		String uri = 
				"http://localhost:8080/rtm/postSLM?dataset=SLA&begin="+ sdf.format(begin) +"&end="+ sdf.format(end) +"&transaction="+ transactionName +"&granularity="+ granularity +"&metric="+ metric +"&exId="+exId;

		PostMethod post = new PostMethod(uri);
		httpclient.executeMethod(post);
		response = post.getResponseBodyAsString();
		System.out.println(response);
		
		/*
		while(rightB.equals(end) || rightB.before(end))
		{
			String uri = 
					"http://localhost:8080/rtm/postSLM?dataset=SLA&begin="+ sdf.format(leftB) +"&end="+ sdf.format(rightB) +"&transaction="+ transactionName +"&granularity="+ granularity +"&metric="+ metric +"&exId="+exId;

			PostMethod post = new PostMethod(uri);
			httpclient.executeMethod(post);
			response = post.getResponseBodyAsString();

			//if(!response.contains("Success"))
			//	System.out.println("Failure : uri=" + uri+ "\nResponse="+ response + "\n");
			
			System.out.println(response);
			
			cal.setTime(leftB);
			Aggregator.addMillisecondsToCalWithLong(cal, gran);
			leftB = cal.getTime();
			Aggregator.addMillisecondsToCalWithLong(cal, gran-1);
			rightB = cal.getTime();
			
			//Thread.currentThread().sleep(1000);
		}
*/
	}

}
