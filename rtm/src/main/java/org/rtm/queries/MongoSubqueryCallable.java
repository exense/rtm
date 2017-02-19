package org.rtm.queries;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.bson.Document;
import org.rtm.buckets.RangeBucket;
import org.rtm.commons.MeasurementConstants;
import org.rtm.db.DBClient;
import org.rtm.requests.guiselector.Selector;
import org.rtm.struct.Dimension;

import com.mongodb.BasicDBObject;

public class MongoSubqueryCallable implements Callable<Dimension>{

	private Document query;
	private RangeBucket<Long> bucket;
	private Properties prop;

	public MongoSubqueryCallable(List<Selector> sel, RangeBucket<Long> bucket,Properties requestProp) {
		
		this.bucket = bucket;
		this.prop = requestProp;
		this.query = new MongoQuery(MongoQuery.selectorsToQuery(sel));
	}

	@Override
	public Dimension call() throws Exception {
		
		Document completeQuery = mergeTimelessWithTimeCriterion(query, buildTimeCriterion(bucket));
		DBClient db = new DBClient();
		
		return new CursorHandler().handle(
										db.executeQuery(completeQuery),
										bucket);
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
