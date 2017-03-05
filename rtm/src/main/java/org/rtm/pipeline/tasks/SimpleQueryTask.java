package org.rtm.pipeline.tasks;

import java.util.List;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.measurement.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class SimpleQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;

	public SimpleQueryTask(List<Selector> sel, MeasurementAccumulator accumulator){
		this.sel = sel;
		this.accumulator = accumulator;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		BsonQuery query = DBClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		return accumulator.handle(new DBClient().executeQuery(query), bucket);
	}
}
