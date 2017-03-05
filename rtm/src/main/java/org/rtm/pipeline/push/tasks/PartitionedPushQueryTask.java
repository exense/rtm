package org.rtm.pipeline.push.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.push.PushPipeline;
import org.rtm.pipeline.push.builders.PushQueryBuilder;
import org.rtm.pipeline.tasks.SharingPartitionedQueryTask;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;

public class PartitionedPushQueryTask extends SharingPartitionedQueryTask{
	
	public PartitionedPushQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize, long timeoutSecs) {
		super(sel, prop, partitioningFactor, poolSize, timeoutSecs);
	}

	@Override
	protected void executeParallel(RangeBucket<Long> bucket) throws Exception{
		PushQueryBuilder builder = new PushQueryBuilder(
				bucket.getLowerBound(),
				bucket.getUpperBound(),
				subsize,
				sel,
				this.accumulator);
				
		new PushPipeline(
				builder,
				super.poolSize,
				super.resultHandler,
				BlockingMode.BLOCKING).execute();
	}
}
