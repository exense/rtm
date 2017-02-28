package org.rtm.pipeline.execute.callable;

public class SharedContextExecution {}
/*
public class SharedContextExecution extends RangeExecute {

	private AccumulationContext sc;
	
	public SharedContextExecution(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp, AccumulationContext sc) {
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
	
}*/
