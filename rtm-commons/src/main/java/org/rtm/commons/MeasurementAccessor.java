package org.rtm.commons;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import step.core.collections.*;

public class MeasurementAccessor implements TransportClient {

	private static final Logger logger = LoggerFactory.getLogger(MeasurementAccessor.class);

	public static final String ENTITY_NAME = "measurements";

	private Collection<Document> coll;

	public MeasurementAccessor(Collection<Document> coll){
		this.coll = coll;
	}

	public void sendStructuredMeasurement(Map<String, Object> m){
		saveMeasurementInternal(new Document(m));
	}

	public void sendStructuredMeasurement(String m){
		saveMeasurementInternal(MeasurementDBConverter.convertToMongo(m));
	}

	private void saveMeasurementInternal(Document m){
		coll.save(m);
	}

	public void saveManyMeasurements(List<Object> lm){
		saveManyMeasurementsInternal(MeasurementDBConverter.convertManyToMongo(lm));
	}


	public void saveManyMeasurementsInternal(List<Document> lm){
		coll.save(lm);
	}

	public void removeManyViaPattern(Filter f){
		coll.remove(f);
	}

	public long getMeasurementCount(){
		return coll.find(Filters.empty(),null,null,null,0).count();
	}

	public Stream<Document> find(Filter filter){
		return coll.find(filter,null,null,null,0);
	}

	public Stream<Document> advancedFind(Filter filter, List<String> fields ){
		return coll.findReduced(filter,null,null,null,0,fields);
	}

	public Stream<Document> find(Filter filter, SearchOrder sortOrder){
		return coll.find(filter,sortOrder,null,null,0);
	}

	public Stream<Document> find(Filter filter, SearchOrder sortOrder, int skip, int limit){
		return coll.find(filter,sortOrder,skip,limit,0);
	}

	public Iterable<String> distinct(String distinctField, Filter filter) {
		return coll.distinct(distinctField,filter);
	}

	@Override
	public void close() {
		// we actually want to keep it alive for the whole JVM lifetime 
		
	}
}
