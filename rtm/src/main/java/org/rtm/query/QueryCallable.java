package org.rtm.query;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;

import org.rtm.db.DBClient;
import org.rtm.request.selection.Selector;
import org.rtm.stream.TimeValue;
import org.rtm.time.RangeBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class QueryCallable implements Callable<TimeValue>{
	
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
	public TimeValue call() throws Exception {
		return produceValueForRange();
	}
	
	public TimeValue produceValueForRange(){
		return new IterableHandler().handle(
				new DBClient().executeQuery(query),
				bucket,
				prop);
	}
}
