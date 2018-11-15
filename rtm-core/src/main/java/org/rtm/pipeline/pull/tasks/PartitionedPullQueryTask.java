package org.rtm.pipeline.pull.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.commons.tasks.MergingPartitionedQueryTask;
import org.rtm.pipeline.pull.PullPipeline;
import org.rtm.pipeline.pull.builders.pipeline.PullPipelineBuilder;
import org.rtm.pipeline.pull.builders.pipeline.SimplePullPipelineBuilder;
import org.rtm.pipeline.pull.builders.query.PullQueryBuilder;
import org.rtm.pipeline.pull.builders.task.PullTaskBuilder;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;

public class PartitionedPullQueryTask extends MergingPartitionedQueryTask{

	//private static final Logger logger = LoggerFactory.getLogger(PartitionedPullQueryTask.class);

	public PartitionedPullQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int subPoolSize, long timeoutSecs) {
		super(sel, prop, partitioningFactor, subPoolSize, timeoutSecs);
	}

	@Override
	protected void executeParallel(RangeBucket<Long> bucket) throws Exception {

		PullTaskBuilder tb = new PullQueryBuilder(super.sel, super.prop);

		PullPipelineBuilder ppb = new SimplePullPipelineBuilder(
				bucket.getLowerBound(),
				bucket.getUpperBound(),
				super.subsize, super.resultHandler, tb);

		PullPipeline pp = new PullPipeline(ppb, poolSize, super.timeoutSecs, BlockingMode.BLOCKING);

		pp.execute();
	}

}
