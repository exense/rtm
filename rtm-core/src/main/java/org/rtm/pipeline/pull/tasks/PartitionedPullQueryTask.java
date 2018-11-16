package org.rtm.pipeline.pull.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.commons.tasks.MergingPartitionedQueryTask;
import org.rtm.pipeline.pull.PullPipelineExecutor;
import org.rtm.pipeline.pull.builders.pipeline.RunableBuilder;
import org.rtm.pipeline.pull.builders.pipeline.PullRunableBuilder;
import org.rtm.pipeline.pull.builders.query.QueryTaskBuilder;
import org.rtm.pipeline.pull.builders.task.RangeTaskBuilder;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;

public class PartitionedPullQueryTask extends MergingPartitionedQueryTask{

	//private static final Logger logger = LoggerFactory.getLogger(PartitionedPullQueryTask.class);

	public PartitionedPullQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int subPoolSize, long timeoutSecs) {
		super(sel, prop, partitioningFactor, subPoolSize, timeoutSecs);
	}

	@Override
	protected void executeParallel(RangeBucket<Long> bucket) throws Exception {

		/* SHIP TO WORKER */
		
		//RangeTaskBuilder tb = new PullQueryBuilder(super.sel, super.accumulator);
		RangeTaskBuilder tb = new QueryTaskBuilder(super.sel, super.prop);

		RunableBuilder ppb = new PullRunableBuilder(
				bucket.getLowerBound(),
				bucket.getUpperBound(),
				super.subsize, super.resultHandler, tb);

		PullPipelineExecutor pp = new PullPipelineExecutor(ppb, poolSize, super.timeoutSecs, BlockingMode.BLOCKING);

		pp.execute();
		
		/**/
	}

}
