package org.rtm.pipeline.pull.builders.query;

import java.util.List;

import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.pipeline.commons.tasks.simple.SimpleQueryTask;
import org.rtm.pipeline.pull.builders.task.RangeTaskBuilder;
import org.rtm.selection.Selector;

public class PullQueryBuilder implements RangeTaskBuilder{
	private List<Selector> selectors;
	private MeasurementAccumulator accumulator;

	public PullQueryBuilder(List<Selector> selectors, MeasurementAccumulator accumulator){
		this.selectors = selectors;
		this.accumulator = accumulator;
	}
	
	@Override
	public RangeTask build() {
		//return new DistributedQueryTask(this.selectors, this.prop);
		return new SimpleQueryTask(this.selectors, this.accumulator);
	}

}
