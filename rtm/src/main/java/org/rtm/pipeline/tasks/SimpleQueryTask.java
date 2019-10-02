package org.rtm.pipeline.tasks;

import java.util.List;

import org.rtm.db.BsonQuery;
import org.rtm.db.QueryClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.mongodb.client.MongoCursor;

public class SimpleQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected MeasurementAccumulator accumulator;

	public SimpleQueryTask(List<Selector> sel, MeasurementAccumulator accumulator){
		this.sel = sel;
		this.accumulator = accumulator;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		BsonQuery query = QueryClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		
		Iterable it = new QueryClient().executeQuery(query);
		MongoCursor iterator = (MongoCursor)it.iterator();
		
		accumulator.handle(lrv, it);

		iterator.close();
		return lrv;
	}
}
