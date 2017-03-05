package org.rtm.pipeline.tasks;

import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;

public abstract class AbstractProduceMergeTask implements RangeTask {

	@Override 
	public LongRangeValue perform(RangeBucket<Long> bucket) throws Exception{
		produce(bucket);
		LongRangeValue lrv = merge(bucket);
		return lrv;
	}

	protected abstract void produce(RangeBucket<Long> bucket) throws Exception;
	protected abstract LongRangeValue merge(RangeBucket<Long> bucket);
}
