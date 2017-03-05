package org.rtm.pipeline.pull.builders;

import org.rtm.pipeline.pull.builders.tasks.PullTaskBuilder;
import org.rtm.pipeline.pull.callables.PullCallable;
import org.rtm.range.OptimisticLongPartitioner;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public class PartitionedPullPipelineBuilder implements PullPipelineBuilder{

	private OptimisticRangePartitioner<Long> orp;
	private ResultHandler rh;
	private PullTaskBuilder tb;
	 
	public PartitionedPullPipelineBuilder(Long start, Long end, Long increment, ResultHandler rh, PullTaskBuilder tb){
		this.orp = new OptimisticLongPartitioner(start, end, increment);
		this.rh = rh;
		this.tb = tb;
	}

	public Runnable buildRunnable() {
		return new PullCallable(this.orp, rh, tb);
	}
}
