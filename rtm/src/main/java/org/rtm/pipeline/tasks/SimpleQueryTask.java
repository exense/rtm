package org.rtm.pipeline.tasks;

import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Stream;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.db.FilterQuery;
import org.rtm.db.QueryClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SimpleQueryTask implements RangeTask {

	private static final Logger logger = LoggerFactory.getLogger(SimpleQueryTask.class);
	private final MeasurementAccessor ma;

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	public SimpleQueryTask(List<Selector> sel, MeasurementAccumulator accumulator, Properties prop, MeasurementAccessor ma){
		this.prop = prop;
		this.sel = sel;
		this.accumulator = accumulator;
		this.ma = ma;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		long start=0;
		if (logger.isTraceEnabled())
			start = System.currentTimeMillis();
		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		FilterQuery query = new QueryClient(prop,ma).buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		long startQuery = System.currentTimeMillis();
		Stream<? extends Map> stream = new QueryClient(prop, ma).executeAdvancedQuery(query.getQuery());
		if (logger.isTraceEnabled()) {
			logger.trace("METRIC - find query: " + (System.currentTimeMillis()-startQuery));
		}
		
		accumulator.handle(lrv, stream, sel);

		if (logger.isTraceEnabled()) {
			logger.trace("performed simple query task: " + (System.currentTimeMillis()-start) + " for  bucket boundaries from " + bucket.getLowerBound() + " to " + bucket.getUpperBound());
		}
		return lrv;
	}
}
