package org.rtm.pipeline.push.callables.execute;

import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangeExecute extends ExecuteCallable {

	@SuppressWarnings("unused")
	private static final Logger logger = LoggerFactory.getLogger(RangeExecute.class);

	protected RangeBucket<Long> bucket;
	protected RangeTask task;

	public RangeExecute(RangeBucket<Long> bucket, RangeTask task) {
		this.bucket = bucket;
		this.task = task;
	}

	@Override
	public LongRangeValue call() throws Exception{
		return task.perform(this.bucket);
	}
}
