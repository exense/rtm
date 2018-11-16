package org.rtm.pipeline.builders.pipeline;

import org.rtm.pipeline.builders.task.RangeTaskBuilder;
import org.rtm.pipeline.runables.RemoteRunable;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public class RemoteRunableBuilder extends PartitionedRunableBuilder{

	public RemoteRunableBuilder(Long start, Long end, Long increment, ResultHandler rh, RangeTaskBuilder tb) {
		super(start, end, increment, rh, tb);
	}

	public Runnable buildRunnable() {
		return new RemoteRunable(super.orp, super.rh, super.tb);
	}
}
