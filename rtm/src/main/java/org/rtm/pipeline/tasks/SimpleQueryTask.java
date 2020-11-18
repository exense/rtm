package org.rtm.pipeline.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.QueryClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.mongodb.client.MongoCursor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleQueryTask implements RangeTask {

	private static final Logger logger = LoggerFactory.getLogger(SimpleQueryTask.class);

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	public SimpleQueryTask(List<Selector> sel, MeasurementAccumulator accumulator, Properties prop){
		this.prop = prop;
		this.sel = sel;
		this.accumulator = accumulator;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		long start=0;
		if (logger.isTraceEnabled())
			start = System.currentTimeMillis();
		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		BsonQuery query = new QueryClient(prop).buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		long startQuery = System.currentTimeMillis();
		Iterable it = new QueryClient(prop).executeAdvancedQuery(query.getQuery());
		MongoCursor iterator = (MongoCursor)it.iterator();
		if (logger.isTraceEnabled()) {
			logger.trace("METRIC - find query: " + (System.currentTimeMillis()-startQuery));
		}
		
		accumulator.handle(lrv, it, sel);

		iterator.close();

		if (logger.isTraceEnabled()) {
			logger.trace("performed simple query task: " + (System.currentTimeMillis()-start) + " for  bucket boundaries from " + bucket.getLowerBound() + " to " + bucket.getUpperBound());
		}
		return lrv;
	}
}
