package org.rtm.metrics.accumulation.base;

public class SumAccumulatorState extends LongBinaryAccumulatorState{
	
	public SumAccumulatorState() {
		super((x,y) -> x+y, 0L);
	}
}
