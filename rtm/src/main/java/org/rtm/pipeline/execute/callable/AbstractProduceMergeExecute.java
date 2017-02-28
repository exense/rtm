package org.rtm.pipeline.execute.callable;

import org.rtm.pipeline.execute.task.IterableTask;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.IterableMeasurementHandler;

public abstract class AbstractProduceMergeExecute extends RangeExecute {

	public AbstractProduceMergeExecute(RangeBucket<Long> bucket, IterableTask task, IterableMeasurementHandler handler) {
		super(bucket, task, handler);
	}
	
	@Override 
	public LongRangeValue call() throws Exception{
		produce();
		LongRangeValue lrv = merge();
		return lrv;
	}

	protected abstract void produce() throws Exception;
	protected abstract LongRangeValue merge();
}
