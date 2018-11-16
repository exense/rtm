package org.rtm.pipeline.commons.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class BackendQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;

	public BackendQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.accumulator = new MeasurementAccumulator(prop);
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		BsonQuery query = DBClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		
		accumulator.handle(lrv, new DBClient().executeQuery(query));
		
		return lrv;
	}
}


