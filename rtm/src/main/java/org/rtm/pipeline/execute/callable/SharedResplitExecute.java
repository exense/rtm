package org.rtm.pipeline.execute.callable;

public class SharedResplitExecute{}/*
public class SharedResplitExecute extends ResplitExecute {

	public SharedResplitExecute(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp,
			long subRangeSize, int parallelizationLevel) {
		super(sel, bucket, requestProp, subRangeSize, parallelizationLevel);
	}

	protected void produce() throws Exception{
		//logger.debug("[" + this.taskId.toString() + "] Producing values now..");
		//pre.processRangeShared();
	}
	
	protected LongRangeValue merge(){
		return super.merge();
	}
	
}
*/