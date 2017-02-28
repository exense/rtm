package org.rtm.pipeline.callable;

import java.util.List;
import java.util.Properties;

import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class SharedParallelQueryCallable extends ParallelQueryCallable {

	public SharedParallelQueryCallable(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp,
			long subRangeSize, int parallelizationLevel) {
		super(sel, bucket, requestProp, subRangeSize, parallelizationLevel);
	}

	protected void produce() throws Exception{
		//logger.debug("[" + this.taskId.toString() + "] Producing values now..");
		//pre.processRangeShared();
	}
	
	protected LongRangeValue merge(){
		return super.merge();
	}
	
}
