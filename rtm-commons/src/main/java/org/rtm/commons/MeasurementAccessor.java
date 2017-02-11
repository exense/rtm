package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MeasurementAccessor {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);

	private static MeasurementAccessor INSTANCE = new MeasurementAccessor();

	private final MongoClient mongo;
	private final MongoDatabase db;
	final MongoCollection<Document> coll;
	
	final String host; 
	final Integer port;
	final String user;
	final String pwd;
	final String database;

	private MeasurementAccessor(){
		
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

	public static MeasurementAccessor getInstance() {
		return INSTANCE;
	}

	public void saveMeasurement(Map<String, Object> m){
		saveMeasurementInternal(new Document(m));
	}

	public void saveMeasurement(String m){
		saveMeasurementInternal(MeasurementConverter.convertToMongo(m));
	}

	private void saveMeasurementInternal(Document m){
		coll.insertOne(m);
	}

	public void saveManyMeasurements(List<Object> lm){
		saveManyMeasurementsInternal(MeasurementConverter.convertManyForInsert(lm));
	}


	public void saveManyMeasurementsInternal(List<Document> lm){
		coll.insertMany(lm);
	}

}
