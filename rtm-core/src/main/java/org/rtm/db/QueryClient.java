package org.rtm.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.SortedSet;
import java.util.TreeSet;

import org.bson.Document;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.selection.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mongodb.BasicDBObject;
import com.mongodb.BasicDBObjectBuilder;
import com.mongodb.client.MongoCursor;

public class QueryClient {

	private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);
	private Properties prop;
	private MeasurementAccessor ma = MeasurementAccessor.getInstance();
	

	public QueryClient(Properties prop) {
		this.ma = MeasurementAccessor.getInstance();
		this.prop = prop;
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

	public BsonQuery buildQuery(List<Selector> sel, LongTimeInterval bucket) {
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		BsonQuery aQuery = new BsonQuery(sel, timeField, timeFormat);
		BsonQuery query = new BsonQuery();
		query.setQuery(mergeTimelessWithTimeCriterion(aQuery.getQuery(), buildTimeCriterion(bucket)));
		return query;
	}

	public Document mergeTimelessWithTimeCriterion(Document timelessQuery, BasicDBObject timeCriterion) {
		Document obj = new Document();
		obj.putAll(timelessQuery);
		obj.putAll(new Document(timeCriterion));
		return obj;
	}

	public BasicDBObject buildTimeCriterion(LongTimeInterval bucket) {
		List<BasicDBObject> criteria = new ArrayList<BasicDBObject>();
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		Object min;
		Object max;
		if(timeFormat.equals("date")) {
			min = new Date(bucket.getBegin());
			max = new Date(bucket.getEnd());
		}else {
			min = bucket.getBegin();
			max = bucket.getEnd();
		}
		criteria.add(new BasicDBObject(timeField, BasicDBObjectBuilder.start("$gte", min).add("$lt", max).get()));
		
		return new BasicDBObject("$and", criteria);
	}

	@SuppressWarnings("rawtypes")
	public LongTimeInterval findEffectiveBoundariesViaMongo(LongTimeInterval lti, List<Selector> sel) throws Exception {
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		
		BsonQuery query = new BsonQuery(sel, timeField, timeFormat);
		
		MongoCursor naturalIt = (MongoCursor)executeQuery(query.getQuery(), timeField, 1).iterator();
		MongoCursor reverseIt = (MongoCursor)executeQuery(query.getQuery(), timeField, -1).iterator();
		Map min = null;
		Map max = null;
		try{
			min = (Map) naturalIt.next();
			max = (Map) reverseIt.next();
		}catch(NoSuchElementException e){
			return new LongTimeInterval(0L, 1L);
		}
		Object maxobject = max.get(timeField);
		Object minobject = min.get(timeField);
		
		Long maxVal = maxobject instanceof Long ? (Long)maxobject : ((Date)maxobject).getTime();
		Long minVal = minobject instanceof Long ? (Long)minobject : ((Date)minobject).getTime();

		long result = maxVal - minVal;
		//logger.debug("time window : " + maxVal + " - " + minVal + " = " + result);

		if(result < 1L)
			throw new Exception("Could not compute auto-granularity : result="+result);

		naturalIt.close();
		reverseIt.close();

		return new LongTimeInterval(minVal, maxVal);
	}

	public static long computeOptimalIntervalSize(long timeWindow, int targetSeriesDots){
		return Math.abs(timeWindow / targetSeriesDots) + 1;
	}

	public long run90PclOnFirstSample(int heuristicSampleSize, List<Selector> sel) {
		logger.debug("Starting sampling of first " + heuristicSampleSize + " data points...");
		long start = System.currentTimeMillis();
		String timeField = (String)prop.get("aggregateService.timeField");
		String timeFormat = (String)prop.get("aggregateService.timeFormat");
		BsonQuery query = new BsonQuery(sel, timeField, timeFormat);

		SortedSet<Long> sortedValues = new TreeSet<>();

		Iterable it = executeQuery(query.getQuery(), timeField, 1, 0, heuristicSampleSize);
		MongoCursor cursor = (MongoCursor) it.iterator();
		Map dot = null;
		for(Object o : it) {
			try{
				dot = (Map) o;
			}catch(Exception e){
				//no more elements
				break;
			}
			if(dot != null) {
				String valueField = (String)prop.get("aggregateService.valueField");
				Object object = dot.get(valueField);
				if(object == null) {
					throw new RuntimeException("No value for valueField:" + valueField);
				}
				sortedValues.add((Long)object);
			}
		}
		cursor.close();
		
		int position = Math.round(sortedValues.size()*0.9F);

		logger.debug("Sampling complete. Duration was "+ (System.currentTimeMillis() - start) +" ms.");

		if(position >= sortedValues.size())
			return sortedValues.last();
		else
			return sortedValues.toArray(new Long[0])[position];
	}
}
