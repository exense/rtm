package org.rtm.pipeline.commons.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class BackendQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;
	protected Properties prop;

	public BackendQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.prop = prop;
		this.accumulator = new MeasurementAccumulator(prop);
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		return new BackendQuery(sel, bucket, this.accumulator, prop).execute();
	}
}


