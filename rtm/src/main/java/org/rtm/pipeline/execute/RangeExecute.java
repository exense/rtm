package org.rtm.pipeline.execute;

import org.rtm.pipeline.task.RangeTask;
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
		
		/**///For test purposes
		
		long sleepTime = 10;
		System.out.println("QueryCallable sleeping for : " + sleepTime +" ms.");
		try {
			Thread.sleep(sleepTime);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}/**/
		
		return task.perform(this.bucket);
	}
}
