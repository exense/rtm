package org.rtm.pipeline.pull.builders.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.pull.PullPipeline;
import org.rtm.pipeline.pull.builders.PullPipelineBuilder;
import org.rtm.pipeline.pull.builders.SimplePipelineBuilder;
import org.rtm.pipeline.tasks.PartitionedQueryTask;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;

public class PartitionedPullQueryTask extends PartitionedQueryTask{

	public PartitionedPullQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize, long timeoutSecs) {
		super(sel, prop, partitioningFactor, poolSize, timeoutSecs);
	}

	@Override
	protected void executeParallel(RangeBucket<Long> bucket) throws Exception {
		
		PullTaskBuilder tb = new PullQueryBuilder(super.sel, super.accumulator);
		
		PullPipelineBuilder ppb = new SimplePipelineBuilder(
				bucket.getLowerBound(),
				bucket.getUpperBound(),
				super.subsize, super.resultHandler, tb);
		
		PullPipeline pp = new PullPipeline(ppb, poolSize, super.timeoutSecs, BlockingMode.BLOCKING);
			
		pp.execute();
	}

}
