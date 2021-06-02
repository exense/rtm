package org.rtm.pipeline.pull.builders.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.pipeline.tasks.SimpleQueryTask;
import org.rtm.request.selection.Selector;

public class PullQueryBuilder implements PullTaskBuilder{
	private List<Selector> selectors;
	private MeasurementAccumulator accumulator;
	private Properties prop;
	private MeasurementAccessor ma;
	
	public PullQueryBuilder(List<Selector> selectors, MeasurementAccumulator accumulator, Properties prop, MeasurementAccessor ma){
		this.prop = prop;
		this.selectors = selectors;
		this.accumulator = accumulator;
		this.ma = ma;
	}
	
	@Override
	public RangeTask build() {
		return new SimpleQueryTask(this.selectors, this.accumulator, prop, ma);
	}

}
