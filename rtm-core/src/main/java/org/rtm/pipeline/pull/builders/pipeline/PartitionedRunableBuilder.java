package org.rtm.pipeline.pull.builders.pipeline;

import org.rtm.pipeline.pull.builders.task.RangeTaskBuilder;
import org.rtm.pipeline.pull.callables.PullRunable;
import org.rtm.range.OptimisticLongPartitioner;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings({"rawtypes", "unused"})
public abstract class PartitionedRunableBuilder implements RunableBuilder{

	protected OptimisticRangePartitioner<Long> orp;
	protected ResultHandler rh;
	protected RangeTaskBuilder tb;
	 
	public PartitionedRunableBuilder(Long start, Long end, Long increment, ResultHandler rh, RangeTaskBuilder tb){
		this.orp = new OptimisticLongPartitioner(start, end, increment);
		this.rh = rh;
		this.tb = tb;
	}

}
