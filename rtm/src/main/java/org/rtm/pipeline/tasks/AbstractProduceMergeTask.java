package org.rtm.pipeline.tasks;

import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractProduceMergeTask implements RangeTask {

	private static final Logger logger = LoggerFactory.getLogger(AbstractProduceMergeTask.class);

	@Override 
	public LongRangeValue perform(RangeBucket<Long> bucket) throws Exception{
		long ts=0;
		if (logger.isTraceEnabled()){
			ts = System.currentTimeMillis();
			logger.trace("Starting processing of ranges");
		}
		produce(bucket);

		if (logger.isTraceEnabled()){
			logger.trace("METRIC - Processed all ranges: " + (System.currentTimeMillis()-ts));
			ts = System.currentTimeMillis();
		}
		LongRangeValue lrv = merge(bucket);

		if (logger.isTraceEnabled()){
			logger.trace("METRIC - Merged all ranges: " + (System.currentTimeMillis()-ts));
		}
		return lrv;
	}

	protected abstract void produce(RangeBucket<Long> bucket) throws Exception;
	protected abstract LongRangeValue merge(RangeBucket<Long> bucket);
}
