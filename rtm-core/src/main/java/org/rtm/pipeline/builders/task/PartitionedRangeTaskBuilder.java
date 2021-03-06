package org.rtm.pipeline.builders.task;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.pipeline.tasks.PartitionedPullQueryTask;
import org.rtm.selection.Selector;

public class PartitionedRangeTaskBuilder implements RangeTaskBuilder{
	private List<Selector> selectors;
	private Properties prop;
	private int subPoolSize;
	private long partitioningFactor;
	private long timeoutSecs;

	public PartitionedRangeTaskBuilder(List<Selector> selectors, Properties prop,
			long partitioningFactor, int subPoolSize, long timeoutSecs){
		this.selectors = selectors;
		this.partitioningFactor = partitioningFactor;
		this.timeoutSecs = timeoutSecs;
		this.subPoolSize = subPoolSize;
		this.prop = prop;
	}
	
	@Override
	public RangeTask build() {
		return new PartitionedPullQueryTask(this.selectors, this.prop, this.partitioningFactor, this.subPoolSize, this.timeoutSecs);
	}

}
