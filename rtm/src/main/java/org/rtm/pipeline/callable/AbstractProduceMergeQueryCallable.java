package org.rtm.pipeline.callable;

import java.util.List;
import java.util.Properties;

import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

public abstract class AbstractProduceMergeQueryCallable extends QueryCallable {

	public AbstractProduceMergeQueryCallable(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp) {
		super(sel, bucket, requestProp);
	}
	
	@Override 
	public LongRangeValue call() throws Exception{
		produce();
		LongRangeValue lrv = merge();
		return lrv;
	}

	protected abstract void produce() throws Exception;
	protected abstract LongRangeValue merge();
}
