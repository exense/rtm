package org.rtm.pipeline.seh.builders;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.execute.task.MongoPartitionedQueryTask;
import org.rtm.pipeline.execute.task.RangeTask;
import org.rtm.request.selection.Selector;

public class SubpartitionedMongoBuilder extends PartitionedBuilder {

	private List<Selector> selectors;
	private Properties prop;
	private long partitioningFactor;
	private int poolSize;
	
	public SubpartitionedMongoBuilder(Long start, Long end, Long increment, List<Selector> selectors, Properties prop,
			long partitioningFactor, int poolSize){
		super(start, end, increment);
		this.selectors = selectors;
		this.prop = prop;
		this.partitioningFactor = partitioningFactor;
		this.poolSize = poolSize;
	}

	@Override
	protected RangeTask createTask() {
		return new MongoPartitionedQueryTask(this.selectors, this.prop, this.partitioningFactor, this.poolSize);
	}

}
