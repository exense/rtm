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

import org.bson.Document;
import org.rtm.commons.Configuration;
import org.rtm.commons.MeasurementDBConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class RTMMongoClient{

	private static final Logger logger = LoggerFactory.getLogger(RTMMongoClient.class);

	private final MongoClient mongoClient;
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

		host = conf.getProperty("db.host");
		Integer confPort = conf.getPropertyAsInteger("db.port");
		port = (confPort==null)?27017:confPort;
		user = conf.getProperty("db.username");
		pwd = conf.getProperty("db.password");
		String confDb = conf.getProperty("db.database");
		database = (confDb==null)?"rtm":confDb;

		ServerAddress address = new ServerAddress(host, port);
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, pwd.toCharArray());
			credentials.add(credential);
		}

		mongoClient = new MongoClient(address, credentials);
		db = mongoClient.getDatabase(database);
		coll = db.getCollection(conf.getProperty("ds.measurements.collectionName"));

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

	public Iterable<Document> selectMeasurements(List<Selector> selectors, int skip, int limit, String sortAttribute) throws Exception {

		String genQuery;

		if(selectors != null && selectors.size() > 0)
			genQuery = MongoQueryBuilder.buildMongoQuery(selectors);
		else
			genQuery = "{}";

		String sort = "{"+sortAttribute+": 1}";
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
		mongoClient.close();
	}
}
