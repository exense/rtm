package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import com.mongodb.WriteResult;

public class MeasurementAccessor {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);
	
	private static MeasurementAccessor INSTANCE = new MeasurementAccessor();
	MongoCollection measurements;
	
	private MeasurementAccessor(){
		try {			
			String host = Configuration.getInstance().getProperty("db.host");
			Integer port = Configuration.getInstance().getPropertyAsInteger("db.port");
			port = port==null?27017:port;
			String user = Configuration.getInstance().getProperty("db.username");
			String pwd = Configuration.getInstance().getProperty("db.password");
			String database = Configuration.getInstance().getProperty("db.database");
			database = database==null?"rtm":database;
			
			ServerAddress address = new ServerAddress(host, port);
			List<MongoCredential> credentials = new ArrayList<MongoCredential>();
			if(user!=null) {
				MongoCredential credential = MongoCredential.createMongoCRCredential(user, database, pwd.toCharArray());
				credentials.add(credential);
			}
			
			MongoClient mongoClient = new MongoClient(address, credentials);
			DB db = mongoClient.getDB(database);
	
			Jongo jongo = new Jongo(db);
			this.measurements = jongo.getCollection(Configuration.getInstance().getProperty("ds.measurements.collectionName"));
		} catch (Exception e) {
			logger.error("An error occurred while initializing the measurement accessor", e);
			throw new RuntimeException(e);
		}
	}
	
    public static MeasurementAccessor getInstance() {
        return INSTANCE;
  }
    
    public WriteResult saveMeasurement(Measurement t){
    	return measurements.save(t);
    }
    
    public WriteResult saveMeasurementsBulk(List<Measurement> lt){
    	return measurements.insert(lt.toArray());
    }

}
