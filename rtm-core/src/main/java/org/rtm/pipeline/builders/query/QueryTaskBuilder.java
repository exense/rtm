package org.rtm.pipeline.builders.query;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.builders.task.RangeTaskBuilder;
import org.rtm.pipeline.commons.tasks.BackendQueryTask;
import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.selection.Selector;

public class QueryTaskBuilder implements RangeTaskBuilder{
	private List<Selector> selectors;
	//private MeasurementAccumulator accumulator;
	private Properties prop;

	//public PullQueryBuilder(List<Selector> selectors, MeasurementAccumulator accumulator){
	public QueryTaskBuilder(List<Selector> selectors, Properties prop){
		this.selectors = selectors;
		//this.accumulator = accumulator;
		this.prop = prop;
	}
	
	@Override
	public RangeTask build() {
		return new BackendQueryTask(this.selectors, this.prop);
		//return new SimpleQueryTask(this.selectors, this.accumulator);
	}

}
