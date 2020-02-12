package org.rtm.commons;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class DBClient implements Closeable{

	private static final Logger logger = LoggerFactory.getLogger(DBClient.class);

	private static DBClient INSTANCE = new DBClient();

	private MongoClient mongo = null;
	private MongoDatabase db = null;
	
	private MongoCollection<Document> measurements = null;
	
	private String host = null; 
	private Integer port = null;
	private String user = null;
	
	private String pwd = null;
	private String database = null;

	private DBClient(){

		Configuration conf = Configuration.getInstance();
		String stringAttempt1 = null;
		String stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.host");	} catch (Exception e) { /* fail silently */	}
		try { stringAttempt2 = conf.getProperty("ds.host"); } catch (Exception e) { /* fail silently */	}
		host = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"localhost":stringAttempt2):stringAttempt1;

		Integer intAttempt1 = null;
		Integer intAttempt2 = null;
		try { intAttempt1 = conf.getPropertyAsInteger("db.port"); } catch (Exception e) { /* fail silently */	}
		try { intAttempt2 = conf.getPropertyAsInteger("ds.port"); } catch (Exception e) { /* fail silently */	}
		port = (intAttempt1 == null || intAttempt1 <= 0)?((intAttempt2 == null || intAttempt2 <= 0)?27017:intAttempt2):intAttempt1;

		stringAttempt1 = null;
		stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.username"); } catch (Exception e) { /* fail silently */	}
		try { stringAttempt2 = conf.getProperty("ds.username"); } catch (Exception e) { /* fail silently */	}
		user = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?null:stringAttempt2):stringAttempt1;

		stringAttempt1 = null;
		stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.password"); } catch (Exception e) { /* fail silently */	}
		try { stringAttempt2 = conf.getProperty("ds.password"); } catch (Exception e) { /* fail silently */	}
		pwd = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?null:stringAttempt2):stringAttempt1;

		stringAttempt1 = null;
		stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.database"); } catch (Exception e) { /* fail silently */	}
		try { stringAttempt2 = conf.getProperty("ds.dbname");  } catch (Exception e) { /* fail silently */	}
		database = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"rtm":stringAttempt2):stringAttempt1;

		ServerAddress address = new ServerAddress(host, port);
		List<MongoCredential> credentials = new ArrayList<MongoCredential>();
		if(user!=null) {
			MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, pwd.toCharArray());
			credentials.add(credential);
		}
		String measurementCollName = null;
		try { measurementCollName = conf.getProperty("ds.measurements.collectionName");   } catch (Exception e) { logger.error("Fatal config issue.", e);}
		
		logger.info("Initializing db with address=" + address + ", credentials=" + credentials + ", database=" + database + ", collection=" + measurementCollName);
		mongo = new MongoClient(address, credentials);
		db = mongo.getDatabase(database);
		measurements = db.getCollection(measurementCollName);
		
		if(mongo == null || db == null || mongo.getAddress() == null || measurements == null)
			logger.error("Mongo is down.");
	}

	public static DBClient getInstance() {
		return INSTANCE;
	}

	public MongoDatabase getDb() {
		return db;
	}

	public void setDb(MongoDatabase db) {
		this.db = db;
	}
	
	public MongoCollection<Document> getMeasurements() {
		return measurements;
	}

	public void setProcegetMeasurementsssors(MongoCollection<Document> measurements) {
		this.measurements = measurements;
	}

	@Override
	public void close() {
		// we actually want to keep it alive for the whole JVM lifetime 
		mongo.close();
	}
}
