package org.rtm.pipeline.pull.builders.pipeline;

import org.rtm.pipeline.pull.builders.task.PullTaskBuilder;
import org.rtm.pipeline.pull.callables.PullCallable;
import org.rtm.range.OptimisticLongPartitioner;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings({"rawtypes", "unused"})
public abstract class PartitionedPullPipelineBuilder implements PullPipelineBuilder{

	protected OptimisticRangePartitioner<Long> orp;
	protected ResultHandler rh;
	protected PullTaskBuilder tb;
	 
	public PartitionedPullPipelineBuilder(Long start, Long end, Long increment, ResultHandler rh, PullTaskBuilder tb){
		this.orp = new OptimisticLongPartitioner(start, end, increment);
		this.rh = rh;
		this.tb = tb;
	}

}
