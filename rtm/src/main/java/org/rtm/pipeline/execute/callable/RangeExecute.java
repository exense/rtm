package org.rtm.pipeline.execute.callable;

import org.rtm.pipeline.execute.task.IterableTask;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.IterableMeasurementHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeExecute extends ExecuteCallable {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(RangeExecute.class);

	protected RangeBucket<Long> bucket;
	protected IterableTask task;
	protected IterableMeasurementHandler handler;

	public RangeExecute(RangeBucket<Long> bucket, IterableTask task, IterableMeasurementHandler handler) {
		this.bucket = bucket;
		this.task = task;
		this.handler = handler;
	}

	@Override
	public LongRangeValue call() throws Exception{
		return handler.handle(task.perform(this.bucket), this.bucket);
	}
}
