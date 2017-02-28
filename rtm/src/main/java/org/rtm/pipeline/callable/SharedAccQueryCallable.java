package org.rtm.pipeline.callable;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.AccumulationContext;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.result.SharingIterableResultHandler;

public class SharedAccQueryCallable extends QueryCallable {

	private AccumulationContext sc;
	
	public SharedAccQueryCallable(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp, AccumulationContext sc) {
		super(sel, bucket, requestProp);
	}

	@Override
	public LongRangeValue call() throws Exception{
		
		AccumulationContext ac = new AccumulationContext(super.bucket);
		new SharingIterableResultHandler(super.prop, sc).handle(
				new DBClient().executeQuery(query),
				bucket);
		ac.outerMerge();
		return ac;
	}
}
