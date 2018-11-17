package org.rtm.pipeline.runables;

import org.rtm.pipeline.builders.task.RangeTaskBuilder;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PullRunable implements Runnable{

	private static final Logger logger = LoggerFactory.getLogger(PullRunable.class);

	protected OptimisticRangePartitioner partitioner;
	protected ResultHandler rh;
	protected RangeTaskBuilder tb;

	public PullRunable(OptimisticRangePartitioner partitioner, ResultHandler rh, RangeTaskBuilder tb){
		this.partitioner = partitioner;
		this.rh = rh;
		this.tb = tb;
	}

	@Override
	public final void run() {
		try {
			RangeBucket bucket;
			while((bucket = splitRange()) != null)
				harvestRange(processRange(bucket));
		} catch (Exception e) {
			logger.debug("Problem during Runable execution.", e );
		}
	}

	public RangeBucket splitRange(){
		return this.partitioner.next();
	}

	public LongRangeValue processRange(RangeBucket bucket) throws Exception{
		if(bucket != null){ // due to optimistic hasNext
			try {
				//logger.debug("Processing bucket " + bucket);
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
