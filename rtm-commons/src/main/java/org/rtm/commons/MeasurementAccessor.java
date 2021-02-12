package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.mongodb.client.model.Projections;
import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;

public class MeasurementAccessor implements TransportClient{

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);

	private static MeasurementAccessor INSTANCE = new MeasurementAccessor();

	private MongoClient mongo = null;
	private MongoDatabase db = null;
	private MongoCollection<Document> coll = null;

	private String host = null; 
	private Integer port = null;
	private String user = null;
	private String pwd = null;
	private String database = null;

	private int batchSize = 1000;

	private MeasurementAccessor(){

		Configuration conf = Configuration.getInstance();
		String stringAttempt1 = null;
		String stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.host");	logger.info("property 'db.host' found and used, value:" + stringAttempt1);} catch (Exception e) { /* fail silently */ logger.info("Property 'db.host' not found."); }
		try { stringAttempt2 = conf.getProperty("ds.host"); logger.info("property 'ds.host' found and used, value:" + stringAttempt2);} catch (Exception e) { /* fail silently */ logger.info("Property 'ds.host' not found.");}
		host = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"localhost":stringAttempt2):stringAttempt1;
		logger.info("Applied value:" + host);
		
		Integer intAttempt1 = null;
		Integer intAttempt2 = null;
		try { intAttempt1 = conf.getPropertyAsInteger("db.port"); 	logger.info("property 'db.port' found and used, value:" + intAttempt1);} catch (Exception e) { /* fail silently */ logger.info("Property 'db.port' not found.");}
		try { intAttempt2 = conf.getPropertyAsInteger("ds.port");   logger.info("property 'ds.host' found and used, value:" + intAttempt2);} catch (Exception e) { /* fail silently */ logger.info("Property 'ds.port' not found.");}
		port = (intAttempt1 == null || intAttempt1 <= 0)?((intAttempt2 == null || intAttempt2 <= 0)?27017:intAttempt2):intAttempt1;
		logger.info("Applied value:" + port);
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
			MongoCredential credential = MongoCredential.createCredential(user, database, pwd.toCharArray());
			credentials.add(credential);
		}
		String collName = null;
		try { collName = conf.getProperty("ds.measurements.collectionName");   } catch (Exception e) { logger.error("Fatal config issue.", e);}
		
		logger.info("Initializing db with address=" + address + ", credentials=" + credentials + ", database=" + database + ", collection=" + collName);
		mongo = new MongoClient(address, credentials);
		db = mongo.getDatabase(database);
		coll = db.getCollection(collName);

		try {
			batchSize = Integer.parseInt(conf.getProperty("ds.measurements.batchSize"));
		} catch (Exception e) {
			logger.info("Batch size not found or incorrect, using default value of " + batchSize + ". Error message: " + e.getMessage()) ;
		}

		if(mongo == null || db == null || mongo.getAddress() == null || coll == null)
			logger.error("Mongo is down.");
	}

	public static MeasurementAccessor getInstance() {
		return INSTANCE;
	}

	public void sendStructuredMeasurement(Map<String, Object> m){
		saveMeasurementInternal(new Document(m));
	}

	public void sendStructuredMeasurement(String m){
		saveMeasurementInternal(MeasurementDBConverter.convertToMongo(m));
	}

	private void saveMeasurementInternal(Document m){
		coll.insertOne(m);
	}

	public void saveManyMeasurements(List<Object> lm){
		saveManyMeasurementsInternal(MeasurementDBConverter.convertManyToMongo(lm));
	}


	public void saveManyMeasurementsInternal(List<Document> lm){
		coll.insertMany(lm);
	}

	public void removeOneViaPattern(Map<String, Object> m){
		coll.deleteOne(new Document(m));
	}

	public void removeManyViaPatternList(List<Map<String, Object>> lm){
		lm.stream().forEach(m -> removeOneViaPattern(m));
	}

	public void removeManyViaPattern(Map<String, Object> m){
		coll.deleteMany(new Document(m));
	}

	public long getMeasurementCount(){
		return coll.count();
	}

	public Iterable<Document> find(Bson filter){
		return coll.find(filter).batchSize(batchSize);
	}

	public Iterable<Document> advancedFind(Bson filter, List<String> fields ){
		return coll.find(filter).projection(Projections.include(fields)).batchSize(batchSize);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder){
		return coll.find(filter).sort(sortOrder);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder, int skip, int limit){
		return coll.find(filter).sort(sortOrder).skip(skip).limit(limit).batchSize(batchSize);
	}

	@Override
	public void close() {
		// we actually want to keep it alive for the whole JVM lifetime 
		mongo.close();
	}
}
