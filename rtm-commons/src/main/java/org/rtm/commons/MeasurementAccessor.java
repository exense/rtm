package org.rtm.commons;

import java.util.List;

import org.jongo.Jongo;
import org.jongo.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.DB;
import com.mongodb.MongoClient;
import com.mongodb.WriteResult;

public class MeasurementAccessor {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);
	
	private static MeasurementAccessor INSTANCE = new MeasurementAccessor();
	MongoCollection measurements;
	
	private MeasurementAccessor(){
		try {
			MongoClient mongoClient = new MongoClient(Configuration.getInstance().getProperty("ds.host"));
			DB db = mongoClient.getDB(Configuration.getInstance().getProperty("ds.dbname"));
	
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
