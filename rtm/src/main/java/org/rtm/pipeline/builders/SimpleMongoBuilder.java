package org.rtm.pipeline.builders;

import java.util.List;

import org.rtm.measurement.MeasurementAccumulator;
import org.rtm.measurement.SharingAccumulator;
import org.rtm.pipeline.task.RangeTask;
import org.rtm.pipeline.task.SimpleQueryTask;
import org.rtm.request.selection.Selector;

public class SimpleMongoBuilder extends PartitionedBuilder {

	private List<Selector> selectors;
	private MeasurementAccumulator accumulator;
	
	public SimpleMongoBuilder(Long start, Long end, Long increment, List<Selector> selectors, MeasurementAccumulator accumulator){
		super(start, end, increment);
		this.selectors = selectors;
		this.accumulator = accumulator;
	}

	@Override
	protected RangeTask createTask() {
		return new SimpleQueryTask(this.selectors, this.accumulator);
	}

}
