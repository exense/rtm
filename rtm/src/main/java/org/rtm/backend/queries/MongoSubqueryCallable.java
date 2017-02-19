package org.rtm.backend.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.bson.Document;
import org.rtm.backend.db.DBClient;
import org.rtm.backend.results.AggregationResult;
import org.rtm.buckets.RangeBucket;
import org.rtm.commons.MeasurementConstants;

import com.mongodb.BasicDBObject;

public class MongoSubqueryCallable implements Callable<AggregationResult>{

	private Document timelessQuery;
	private RangeBucket<Long> bucket;

	public MongoSubqueryCallable(Document timelessQuery, RangeBucket<Long> bucket, Properties requestProp) {
		this.timelessQuery = timelessQuery;
		this.bucket = bucket;
	}

	@Override
	public AggregationResult call() throws Exception {
		
		System.out.println("I'm in!");
		
		return new QueryHandler().handle(
				new DBClient().executeQuery(
						mergeTimelessWithTimeCriterion(timelessQuery, buildTimeCriterion(bucket))
						));
						
	}

	private Document mergeTimelessWithTimeCriterion(Document timelessQuery, BasicDBObject timeCriterion) {
		Document obj = new Document();
		obj.putAll(timelessQuery);
		obj.putAll(new Document(timeCriterion));
		return obj;
	}

	public static BasicDBObject buildTimeCriterion(RangeBucket<Long> bucket) {
		List<BasicDBObject> criteria = new ArrayList<BasicDBObject>();
		criteria.add(new BasicDBObject(MeasurementConstants.BEGIN_KEY, new BasicDBObject("$gte", bucket.getLowerBound())));
		criteria.add(new BasicDBObject(MeasurementConstants.BEGIN_KEY, new BasicDBObject("$lt", bucket.getUpperBound())));
		return new BasicDBObject("$and", criteria);
	}

}
