package org.rtm.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.rtm.db.DBClient;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.time.RangeBucket;
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
		//logger.debug("Executing QueryCallable for bucket="+ bucket);
		return produceValueForRange();
	}

	// Pass accumulators through context to reduce memory footprint
	public LongRangeValue produceValueForRange(){
		return new IterableHandler().handle(
				new DBClient().executeQuery(query),
				bucket,
				prop,
				null);
	}
}
