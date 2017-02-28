package org.rtm.pipeline.callable;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.MergingIterableResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryCallable implements Callable<LongRangeValue>{

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(QueryCallable.class);

	protected BsonQuery query;
	protected RangeBucket<Long> bucket;
	protected Properties prop;
	protected List<Selector> sel;

	public QueryCallable(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp) {
		this.bucket = bucket;
		this.prop = requestProp;
		this.sel = sel;
		this.query = DBClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
	}

	@Override
	public LongRangeValue call() throws Exception{
		return new MergingIterableResultHandler(prop).handle(
				new DBClient().executeQuery(query),
				bucket);
	}
}
