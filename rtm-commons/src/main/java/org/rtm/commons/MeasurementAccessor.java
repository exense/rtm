package org.rtm.commons;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.bson.conversions.Bson;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.client.MongoCollection;

public class MeasurementAccessor implements TransportClient{

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);

	private static MeasurementAccessor INSTANCE = new MeasurementAccessor();

	private DBClient client = DBClient.getInstance();
	private MongoCollection<Document> measurements = client.getMeasurements();

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
		measurements.insertOne(m);
	}

	public void saveManyMeasurements(List<Object> lm){
		saveManyMeasurementsInternal(MeasurementDBConverter.convertManyToMongo(lm));
	}


	public void saveManyMeasurementsInternal(List<Document> lm){
		measurements.insertMany(lm);
	}

	public void removeOneViaPattern(Map<String, Object> m){
		measurements.deleteOne(new Document(m));
	}

	public void removeManyViaPatternList(List<Map<String, Object>> lm){
		lm.stream().forEach(m -> removeOneViaPattern(m));
	}

	public void removeManyViaPattern(Map<String, Object> m){
		measurements.deleteMany(new Document(m));
	}

	public long getMeasurementCount(){
		return measurements.count();
	}

	public Iterable<Document> find(Bson filter){
		return measurements.find(filter);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder){
		return measurements.find(filter).sort(sortOrder);
	}

	public Iterable<Document> find(Bson filter, Bson sortOrder, int skip, int limit){
		return measurements.find(filter).skip(skip).limit(limit);
	}

	@Override
	public void close() {
		// we actually want to keep it alive for the whole JVM lifetime 
		client.close();
	}
}
