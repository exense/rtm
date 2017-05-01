package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

	private MeasurementAccessor(){

		Configuration conf = Configuration.getInstance();
		String stringAttempt1 = null;
		String stringAttempt2 = null;
		try { stringAttempt1 = conf.getProperty("db.host");	} catch (Exception e) { /* fail silently */	}
		try { stringAttempt2 = conf.getProperty("ds.host"); } catch (Exception e) { /* fail silently */	}
		host = (stringAttempt1 == null || stringAttempt1.isEmpty())?((stringAttempt2 == null || stringAttempt2.isEmpty())?"localhost":stringAttempt2):stringAttempt1;

		Integer intAttempt1 = null;
		Integer intAttempt2 = null;
		try { intAttempt1 = conf.getPropertyAsInteger("db.port"); } catch (Exception e) { /* fail silently */	}
		try { intAttempt2 = conf.getPropertyAsInteger("port"); } catch (Exception e) { /* fail silently */	}
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

		logger.info("Initializing db with address=" + address + ", credentials=" + credentials);
		mongo = new MongoClient(address, credentials);
		db = mongo.getDatabase(database);
		String collName = null;
		
		try { collName = conf.getProperty("ds.measurements.collectionName");   } catch (Exception e) { logger.error("Fatal config issue.", e);}
		coll = db.getCollection(collName);

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
		return coll.find(filter);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder){
		return coll.find(filter).sort(sortOrder);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder, int skip, int limit){
		return coll.find(filter).skip(skip).limit(limit);
	}

	@Override
	public void close() {
		// we actually want to keep it alive for the whole JVM lifetime 
		//mongo.close();
	}
}
