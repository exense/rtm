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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.rtm.commons.Configuration;
import org.rtm.commons.MeasurementConstants;
import org.rtm.commons.MeasurementDBConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

@SuppressWarnings({"unchecked", "rawtypes"})
public class RTMMongoClient{

	private static final Logger logger = LoggerFactory.getLogger(RTMMongoClient.class);

	private final MongoClient mongo;
	private final MongoDatabase db;
	final MongoCollection<Document> coll;

	private static RTMMongoClient INSTANCE = new RTMMongoClient();
	private static RTMMongoClient NEW_INSTANCE;

	private static boolean reloadTriggered = false;

	final private String host;
	final private Integer port;
	final private String user;
	final private String pwd;
	final private String database;

	private RTMMongoClient(){
		super();

		Configuration conf = Configuration.getInstance();
		String stringAttempt1 = conf.getProperty("db.host");
		String stringAttempt2 = conf.getProperty("ds.host");
		host = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"localhost":stringAttempt2):stringAttempt1;

		Integer intAttempt1 = conf.getPropertyAsInteger("db.port");
		Integer intAttempt2 = conf.getPropertyAsInteger("port");
		port = (intAttempt1 == null || intAttempt1 <= 0)?((intAttempt2 == null || intAttempt2 <= 0)?27017:intAttempt2):intAttempt1;
		
		stringAttempt1 = conf.getProperty("db.username");
		stringAttempt2 = conf.getProperty("ds.username");
		user = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?null:stringAttempt2):stringAttempt1;
		
		stringAttempt1 = conf.getProperty("db.password");
		stringAttempt2 = conf.getProperty("ds.password");
		pwd = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?null:stringAttempt2):stringAttempt1;
		
		stringAttempt1 = conf.getProperty("db.database");
		stringAttempt2 = conf.getProperty("ds.dbname");
		
		database = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"rtm":stringAttempt2):stringAttempt1;

		ServerAddress address = new ServerAddress(host, port);
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, pwd.toCharArray());
			credentials.add(credential);
		}

		mongo = new MongoClient(address, credentials);
		db = mongo.getDatabase(database);
		coll = db.getCollection(conf.getProperty("ds.measurements.collectionName"));
		
		if(mongo == null || db == null || mongo.getAddress() == null || coll == null)
			logger.error("Mongo is down.");
	}

	public static RTMMongoClient getInstance() {
		if(reloadTriggered)
		{
			synchronized(RTMMongoClient.class)
			{
				INSTANCE = NEW_INSTANCE;
				NEW_INSTANCE = null;
				reloadTriggered = false;
			}
		}
		return INSTANCE;
	}

	public Iterable<Document> selectMeasurements(List<Selector> selectors, int skip, int limit,
			String beginKey) throws Exception {
		return selectMeasurements(selectors, skip, limit, beginKey, "1");
	}

	public Iterable selectMeasurements(List<Selector> selectors, int skip, int limit, String sortAttribute, String sortDirection) throws Exception {

		String genQuery;

		if(selectors != null && selectors.size() > 0)
			genQuery = MongoQueryBuilder.buildMongoQuery(selectors);
		else
			genQuery = "{}";

		String sort = "{"+sortAttribute+": "+sortDirection+"}";
		logger.debug("selectMeasurements: [sort]"+sort+"[find]" + genQuery);

		if(skip > 0){
			if(limit > 0){
				return coll.find(MeasurementDBConverter.convertToMongo(genQuery)).skip(skip).limit(limit).sort(MeasurementDBConverter.convertToMongo(sort));
			}else{
				return coll.find(MeasurementDBConverter.convertToMongo(genQuery)).skip(skip).sort(MeasurementDBConverter.convertToMongo(sort));
			}
		}else{
			if(limit > 0){
				return coll.find(MeasurementDBConverter.convertToMongo(genQuery)).limit(limit).sort(MeasurementDBConverter.convertToMongo(sort));
			}else{
				return coll.find(MeasurementDBConverter.convertToMongo(genQuery)).sort(MeasurementDBConverter.convertToMongo(sort));				
			}
		}
	}

	public static synchronized void triggerReload(){
		NEW_INSTANCE = new RTMMongoClient();
		reloadTriggered = true;
	}

	public void close(){
		mongo.close();
	}

	public long getTimeWindow(Iterable<? extends Map<String, Object>> naturalOrder, Iterable<? extends Map<String, Object>> reverseOrder) throws Exception {
		Map<String, Object> min = naturalOrder.iterator().next();
		Map<String, Object> max = reverseOrder.iterator().next();

		Long maxVal = (Long)max.get(MeasurementConstants.BEGIN_KEY); 
		Long minVal = (Long)min.get(MeasurementConstants.BEGIN_KEY);
		
		long result = maxVal - minVal;
		logger.debug("time window : " + maxVal + " - " + minVal + " = " + result);
		
		if(result < 1L)
			throw new Exception("Could not compute auto-granularity : result="+result);
		
		return result;
	}


}
