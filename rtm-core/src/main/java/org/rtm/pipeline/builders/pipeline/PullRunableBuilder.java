package org.rtm.pipeline.builders.pipeline;

import org.rtm.pipeline.builders.task.RangeTaskBuilder;
import org.rtm.pipeline.runables.PullRunable;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public class PullRunableBuilder extends PartitionedRunableBuilder{

	public PullRunableBuilder(Long start, Long end, Long increment, ResultHandler rh, RangeTaskBuilder tb) {
		super(start, end, increment, rh, tb);
	}

	public Runnable buildRunnable() {
		return new PullRunable(super.orp, super.rh, super.tb);
	}
}
