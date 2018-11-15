package org.rtm.pipeline.commons.tasks.distributed;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.pipeline.commons.tasks.RangeTask;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class DistributedQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;

	public DistributedQueryTask(List<Selector> sel, Properties prop){
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


