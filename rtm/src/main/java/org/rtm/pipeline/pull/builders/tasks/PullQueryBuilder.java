package org.rtm.pipeline.pull.builders.tasks;

import java.util.List;

import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.pipeline.tasks.SimpleQueryTask;
import org.rtm.request.selection.Selector;

public class PullQueryBuilder implements PullTaskBuilder{
	private List<Selector> selectors;
	private MeasurementAccumulator accumulator;

	public PullQueryBuilder(List<Selector> selectors, MeasurementAccumulator accumulator){
		this.selectors = selectors;
		this.accumulator = accumulator;
	}
	
	@Override
	public RangeTask build() {
		return new SimpleQueryTask(this.selectors, this.accumulator);
	}

}
