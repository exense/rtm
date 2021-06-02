package org.rtm.pipeline.pull.builders.tasks;

import java.util.List;
import java.util.Properties;

import ch.exense.commons.app.Configuration;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.request.selection.Selector;

public class PartitionedPullQueryBuilder implements PullTaskBuilder{
	private final Configuration configuration;
	private List<Selector> selectors;
	private Properties prop;
	private int subPoolSize;
	private long partitioningFactor;
	private long timeoutSecs;
	private MeasurementAccessor ma;
	
	public PartitionedPullQueryBuilder(List<Selector> selectors, Properties prop,
									   long partitioningFactor, int subPoolSize, long timeoutSecs,
									   MeasurementAccessor ma, Configuration configuration){
		this.selectors = selectors;
		this.partitioningFactor = partitioningFactor;
		this.timeoutSecs = timeoutSecs;
		this.subPoolSize = subPoolSize;
		this.prop = prop;
		this.ma = ma;
		this.configuration = configuration;
	}
	
	@Override
	public RangeTask build() {
		return new PartitionedPullQueryTask(this.selectors, this.prop, this.partitioningFactor, this.subPoolSize, this.timeoutSecs, ma, configuration);
	}

}
