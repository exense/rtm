package org.rtm.pipeline.builders.pull;

import java.util.List;

import org.rtm.measurement.MeasurementAccumulator;
import org.rtm.pipeline.task.RangeTask;
import org.rtm.pipeline.task.SimpleQueryTask;
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
