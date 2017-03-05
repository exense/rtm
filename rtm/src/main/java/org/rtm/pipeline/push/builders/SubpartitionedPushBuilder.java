package org.rtm.pipeline.push.builders;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.push.tasks.PartitionedPushQueryTask;
import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.request.selection.Selector;

public class SubpartitionedPushBuilder extends PartitionedPushBuilder {

	private List<Selector> selectors;
	private Properties prop;
	private long partitioningFactor;
	private int poolSize;	
	
	public SubpartitionedPushBuilder(Long start, Long end, Long increment, List<Selector> selectors, Properties prop,
			long partitioningFactor, int poolSize){
		super(start, end, increment);
		this.selectors = selectors;
		this.prop = prop;
		this.partitioningFactor = partitioningFactor;
		this.poolSize = poolSize;
	}

	@Override
	protected RangeTask createTask() {
		return new PartitionedPushQueryTask(this.selectors, this.prop, this.partitioningFactor, this.poolSize);
	}

}
