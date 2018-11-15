package org.rtm.pipeline.pull.builders.pipeline;

import org.rtm.pipeline.pull.builders.task.PullTaskBuilder;
import org.rtm.pipeline.pull.callables.PullCallable;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public class SimplePullPipelineBuilder extends PartitionedPullPipelineBuilder{

	public SimplePullPipelineBuilder(Long start, Long end, Long increment, ResultHandler rh, PullTaskBuilder tb) {
		super(start, end, increment, rh, tb);
	}

	public Runnable buildRunnable() {
		return new PullCallable(super.orp, super.rh, super.tb);
	}
}
