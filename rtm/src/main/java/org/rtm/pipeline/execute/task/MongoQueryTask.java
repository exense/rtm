package org.rtm.pipeline.execute.task;

import java.util.List;
import java.util.Map;

import org.bson.Document;
import org.rtm.db.BsonQuery;
import org.rtm.db.DBClient;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;

@SuppressWarnings("rawtypes")
public class MongoQueryTask implements IterableTask {

	protected BsonQuery query;
	protected List<Selector> sel;

	public MongoQueryTask(List<Selector> sel){
		this.sel = sel;
	}

	@Override
	public Iterable<? extends Map> perform(RangeBucket<Long> bucket) {
		this.query = DBClient.buildQuery(sel, RangeBucket.toLongTimeInterval(bucket));
		return new DBClient().executeQuery(query);
	}
}
