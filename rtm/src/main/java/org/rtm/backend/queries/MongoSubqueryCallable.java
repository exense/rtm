package org.rtm.backend.queries;

import java.util.Properties;
import java.util.concurrent.Callable;

import org.bson.Document;
import org.rtm.backend.db.DBClient;
import org.rtm.backend.results.AggregationResult;
import org.rtm.buckets.RangeBucket;

public class MongoSubqueryCallable implements Callable<AggregationResult>{

	private Document timelessQuery;
	private DBClient tc;

	public MongoSubqueryCallable(Document timelessQuery, DBClient tc, Properties requestProp) {
		this.timelessQuery = timelessQuery;
		this.tc = tc;
	}

	@Override
	public AggregationResult call() throws Exception {
		return new QueryHandler(tc.executeQuery(timelessQuery)).handle();
	}

	public static Document buildTimeQuery(RangeBucket<Long> next) {
		// TODO Auto-generated method stub
		return null;
	}

}
