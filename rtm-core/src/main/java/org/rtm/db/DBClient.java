package org.rtm.db;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bson.Document;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.MeasurementConstants;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.selection.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.client.MongoCursor;

public class DBClient {

	private static final Logger logger = LoggerFactory.getLogger(DBClient.class);

	MeasurementAccessor ma = MeasurementAccessor.getInstance();

	public DBClient() {
		this.ma = MeasurementAccessor.getInstance();
	}

	@SuppressWarnings("rawtypes")
	public Iterable<? extends Map> executeQuery(Document timelessQuery) {
		return ma.find(timelessQuery);
	}

	@SuppressWarnings("rawtypes")
	public Iterable<? extends Map> executeQuery(Document timelessQuery, String sortKey, Integer sortDirection) {
		return ma.find(timelessQuery, new BasicDBObject(sortKey, sortDirection));
	}

	@SuppressWarnings("rawtypes")
	public Iterable<? extends Map> executeQuery(Document timelessQuery, String sortKey, Integer sortDirection, int skip, int limit) {
		return ma.find(timelessQuery, new BasicDBObject(sortKey, sortDirection), skip, limit);
	}

	public static BsonQuery buildQuery(List<Selector> sel, LongTimeInterval bucket) {
		BsonQuery aQuery = new BsonQuery(BsonQuery.selectorsToQuery(sel));
		return new BsonQuery(mergeTimelessWithTimeCriterion((Document)aQuery, buildTimeCriterion(bucket)));
	}

	public static Document mergeTimelessWithTimeCriterion(Document timelessQuery, BasicDBObject timeCriterion) {
		Document obj = new Document();
		obj.putAll(timelessQuery);
		obj.putAll(new Document(timeCriterion));
		return obj;
	}

	public static BasicDBObject buildTimeCriterion(LongTimeInterval bucket) {
		List<BasicDBObject> criteria = new ArrayList<BasicDBObject>();
		criteria.add(new BasicDBObject(MeasurementConstants.BEGIN_KEY, new BasicDBObject("$gte", bucket.getBegin())));
		criteria.add(new BasicDBObject(MeasurementConstants.BEGIN_KEY, new BasicDBObject("$lt", bucket.getEnd())));
		return new BasicDBObject("$and", criteria);
	}

	@SuppressWarnings("rawtypes")
	public static LongTimeInterval findEffectiveBoundariesViaMongo(LongTimeInterval lti, List<Selector> sel) throws Exception {
		BsonQuery query = new BsonQuery(BsonQuery.selectorsToQuery(sel));

		DBClient db = new DBClient();

		//TODO: get Time key from request Properties
		//logger.debug(completeQuery.toString());
		Iterable naturalIt = db.executeQuery(query, MeasurementConstants.BEGIN_KEY, 1);
		Iterable reverseIt = db.executeQuery(query, MeasurementConstants.BEGIN_KEY, -1);
		Map min = null;
		Map max = null;
		try{
			min = (Map) naturalIt.iterator().next();
			max = (Map) reverseIt.iterator().next();
		}catch(NoSuchElementException e){
			return new LongTimeInterval(0L, 1L);
		}

		Long maxVal = (Long)max.get(MeasurementConstants.BEGIN_KEY) +1; // $lt operator 
		Long minVal = (Long)min.get(MeasurementConstants.BEGIN_KEY);

		long result = maxVal - minVal;
		//logger.debug("time window : " + maxVal + " - " + minVal + " = " + result);

		if(result < 1L)
			throw new Exception("Could not compute auto-granularity : result="+result);

		((MongoCursor) naturalIt.iterator()).close();
		((MongoCursor) reverseIt.iterator()).close();

		return new LongTimeInterval(minVal, maxVal);
	}

	public static long computeOptimalIntervalSize(long timeWindow, int targetSeriesDots){
		return Math.abs(timeWindow / targetSeriesDots) + 1;
	}

	public static long run90PclOnFirstSample(int heuristicSampleSize, List<Selector> sel) {
		logger.info("Starting sampling of first " + heuristicSampleSize + " data points...");
		long start = System.currentTimeMillis();
		BsonQuery query = new BsonQuery(BsonQuery.selectorsToQuery(sel));

		DBClient db = new DBClient();

		SortedSet<Long> sortedValues = new TreeSet<>();
		
		Iterator naturalIt = db.executeQuery(query, MeasurementConstants.BEGIN_KEY, 1).iterator();
		int i = 0;
		while(i < heuristicSampleSize){
			Map dot = null;
			try{
			dot = (Map) naturalIt.next();
			}catch(Exception e){
				//no more elements
				break;
			}
			sortedValues.add((Long)dot.get(MeasurementConstants.VALUE_KEY));
			i++;
		}
		((MongoCursor) naturalIt).close();
		
		int position = Math.round(sortedValues.size()*0.9F);
		
		logger.info("Sampling complete. Duration was "+ (System.currentTimeMillis() - start) +" ms.");
		
		if(position >= sortedValues.size())
			return sortedValues.last();
		else
			return sortedValues.toArray(new Long[0])[position];
	}
}
