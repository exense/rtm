package org.rtm.pipeline.tasks;

import java.util.List;
import java.util.Properties;

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
	protected Properties prop;

	public SimpleQueryTask(List<Selector> sel, MeasurementAccumulator accumulator, Properties prop){
		this.prop = prop;
		this.sel = sel;
		this.accumulator = accumulator;
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		LongRangeValue lrv = new LongRangeValue(bucket.getLowerBound());
		BsonQuery query = new QueryClient(prop).buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		
		Iterable it = new QueryClient(prop).executeQuery(query.getQuery());
		MongoCursor iterator = (MongoCursor)it.iterator();
		
		accumulator.handle(lrv, it, sel);

		iterator.close();
		return lrv;
	}
}
