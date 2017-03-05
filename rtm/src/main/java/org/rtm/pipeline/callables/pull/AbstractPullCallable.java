package org.rtm.pipeline.callables.pull;

import org.rtm.pipeline.builders.pull.PullTaskBuilder;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public abstract class AbstractPullCallable implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(AbstractPullCallable.class);
	
	protected OptimisticRangePartitioner partitioner;
	protected ResultHandler rh;
	protected PullTaskBuilder tb;
	
	public AbstractPullCallable(OptimisticRangePartitioner partitioner, ResultHandler rh, PullTaskBuilder tb){
		this.partitioner = partitioner;
		this.rh = rh;
		this.tb = tb;
	}
	
	@Override
	public final void run() {
		try {
			harvestRange(
					processRange(
							splitRange()));
		} catch (Exception e) {
			logger.debug("Problem during pipeline execution.", e );
		}
	}

	public RangeBucket splitRange(){
		return this.partitioner.next();
	}
	
	public LongRangeValue processRange(RangeBucket bucket) throws Exception{
		if(bucket != null){ // due to optimistic hasNext
			try {
				return tb.build().perform(bucket);
			} catch (Exception e) {
				logger.debug("Execution failed for bucket: " + bucket, e);
				throw e;
			}
		}
		return null;
	}
	
	public void harvestRange(LongRangeValue lrv){
		this.rh.attachResult(lrv);
	}

}
