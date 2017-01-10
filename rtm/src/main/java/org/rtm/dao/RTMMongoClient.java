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
package org.rtm.dao;

import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.exception.RTMException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;

public class RTMMongoClient{

	private static final Logger logger = LoggerFactory.getLogger(RTMMongoClient.class);

	MongoCollection measurements;
	MongoCollection aggregates;

	private static RTMMongoClient INSTANCE = new RTMMongoClient();
	private static RTMMongoClient NEW_INSTANCE;

	private static Boolean chooseMeForReferenceUpdate = true;
	private static Boolean chooseMeForReloadTrigger = true;

	private static boolean reloadTriggered = false;
	
	private boolean isDebug = false;

	private RTMMongoClient(){
		super();

		String measurementsColl = Configuration.getInstance().getProperty(Configuration.MEASUREMENTSCOLL_KEY);
		
		/* Currently not needed - until we manipulate and save aggregates again*/
		//String aggregatesColl = Configuration.getInstance().getProperty("ds.aggregates.collectionName");

		if(Configuration.getInstance().getProperty(Configuration.DEBUG_KEY) != null)
			this.isDebug = Configuration.getInstance().getProperty(Configuration.DEBUG_KEY).equals("true");

		String host = Configuration.getInstance().getProperty("db.host");
		Integer port = Configuration.getInstance().getPropertyAsInteger("db.port");
		port = port==null?27017:port;
		String user = Configuration.getInstance().getProperty("db.username");
		String pwd = Configuration.getInstance().getProperty("db.password");
		String database = Configuration.getInstance().getProperty("db.database");
		database = database==null?"rtm":database;
		
		MongoClient mongoClient;
		ServerAddress address = new ServerAddress(host, port);
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, pwd.toCharArray());
			credentials.add(credential);
		}
		
		mongoClient = new MongoClient(address, credentials);
		DB db = mongoClient.getDB(database);
		if(db != null)
			System.out.println("Connected to: " + mongoClient.getAddress().getHost() + "; db=" + database);
		else
			System.out.println("DB is null");
		Jongo jongo = new Jongo(db);

		this.measurements = jongo.getCollection(measurementsColl);
		if(this.measurements != null)
			System.out.println("Got handle on collection: " + measurementsColl);
		else
			System.out.println("measurements is null");

		/*
		this.aggregates = jongo.getCollection(aggregatesColl);
		if(this.aggregates != null)
			System.out.println("Got handle on collection: " + aggregatesColl);
		else
			System.out.println("aggregates is null");
		 */
	}

	public static RTMMongoClient getInstance() {
		if(reloadTriggered)
		{
			boolean wasIChosen = false;

			synchronized(chooseMeForReferenceUpdate){
				if(chooseMeForReferenceUpdate == true){
					chooseMeForReferenceUpdate = false;
					wasIChosen = true;
				}
			}

			if(wasIChosen){
				synchronized(Thread.currentThread().getClass())
				{
					INSTANCE = NEW_INSTANCE;
					NEW_INSTANCE = null;
					reloadTriggered = false;
					chooseMeForReferenceUpdate = true;
				}
			}
		}
		return INSTANCE;
	}

	public static String buildQuery(List<Selector> selectors, List<Object> result) throws Exception {

		final String textPrefix = Configuration.TEXT_PREFIX + Configuration.SPLITTER;
		final String numPrefix = Configuration.NUM_PREFIX + Configuration.SPLITTER;
		
		int sltSize = selectors.size();
		StringBuilder genQuerySb = new StringBuilder();													// {

		if(sltSize != 0){


			if(sltSize > 1)
				genQuerySb.append("{$or : [");											// { $or : [

			for(Selector slt : selectors)
			{
				if(slt == null)
					throw new Exception("Selector is null :" + slt);
				else
				{
					genQuerySb.append("{");												// { $or : [ {

					if(slt.hasTextFilters())
					{
						List<TextFilter> textFilters = slt.getTextFilters();
						for(TextFilter f : textFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							genQuerySb.append("\"");
							genQuerySb.append(textPrefix+f.getKey());
							genQuerySb.append("\":");
							
							if(f.isRegex())
								genQuerySb.append("{$regex : #}");
							else
								genQuerySb.append("#");
							
							result.add(f.getValue());
							genQuerySb.append(",");
						}

					}


					if(slt.hasNumericalFilters())
					{
						List<NumericalFilter> numericalFilters = slt.getNumericalFilters();
						for(NumericalFilter f : numericalFilters)											// { $or : [ { "toto" : "tutu", "allo" :"alhuile",
						{
							if(f.hasMaxValue() || f.hasMinValue()){
								genQuerySb.append("\""+numPrefix+f.getKey()+"\": {");

								if(f.hasMaxValue()){
									result.add(f.getMaxValue());
									genQuerySb.append("$lt : #");
									if(f.hasMinValue()){
										genQuerySb.append(",");
									}
								}
								if(f.hasMinValue()){
									result.add(f.getMinValue());
									genQuerySb.append("$gte : #");
								}

								genQuerySb.append("},");
							}

						}
						genQuerySb.deleteCharAt(genQuerySb.length() - 1);
					}// End If Num Filters

				} // End If Selector null

				genQuerySb.append("},");
			} // End For Selectors
			if(sltSize > 1)
			{
				genQuerySb.deleteCharAt(genQuerySb.length() - 1);
				genQuerySb.append("]}");
			}

		}
		return genQuerySb.toString();
	}

	public Iterable<Measurement> selectMeasurements(List<Selector> selectors, int skip, int limit, String sortAttribute) throws Exception {

		List<Object> paramArray = new ArrayList<Object>();
		String genQuery;

		if(selectors != null && selectors.size() > 0)
			genQuery = buildQuery(selectors, paramArray);
		else
			genQuery = "{}";

		String sort = "{"+sortAttribute+": 1}";
		if(isDebug)
			System.out.println("selectMeasurements: [sort]"+sort+"[find]" + genQuery + "[paramArray]" + paramArray);
		if(skip > 0){
			if(limit > 0){
				return measurements.find(genQuery, paramArray.toArray()).skip(skip).limit(limit).sort(sort).as(Measurement.class);
			}else{
				return measurements.find(genQuery, paramArray.toArray()).skip(skip).sort(sort).as(Measurement.class);
			}
		}else{
			if(limit > 0){
				return measurements.find(genQuery, paramArray.toArray()).limit(limit).sort(sort).as(Measurement.class);
			}else{
				return measurements.find(genQuery, paramArray.toArray()).sort(sort).as(Measurement.class);				
			}
		}
	}

	public void triggerReload() throws RTMException {
		
		boolean wasIChosen = false;

		synchronized(chooseMeForReloadTrigger){
			//System.out.println(Thread.currentThread().getId() + ": I was chosen for reload trigger !");
			if(chooseMeForReloadTrigger == true){
				chooseMeForReloadTrigger = false;
				wasIChosen = true;
			}
		}
		if(wasIChosen){
			//System.out.println("I'm triggering a reload !");
			NEW_INSTANCE = new RTMMongoClient();
			reloadTriggered = true;
			
			synchronized(chooseMeForReloadTrigger){	
				chooseMeForReloadTrigger = true;
			}
		}else{
			throw new RTMException("Reload failed : the configuration is already currently being reloaded.");
		}
		
	}
}
