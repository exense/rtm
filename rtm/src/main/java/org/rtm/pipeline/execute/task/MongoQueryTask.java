package org.rtm.pipeline.execute.task;

import java.util.List;
import java.util.Properties;

import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.IterableMeasurementHandler;
import org.rtm.stream.result.MergingIterableResultHandler;

public class MongoQueryTask implements RangeTask {

	protected List<Selector> sel;
	protected IterableMeasurementHandler handler;
	protected Properties prop;

	public MongoQueryTask(List<Selector> sel, Properties prop){
		this.sel = sel;
		this.prop = prop;
		this.handler = new MergingIterableResultHandler(this.prop);
	}

	@Override
	public LongRangeValue perform(RangeBucket<Long> bucket) {
		BsonQuery query = DBClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		return handler.handle(new DBClient().executeQuery(query), bucket);
	}
}
