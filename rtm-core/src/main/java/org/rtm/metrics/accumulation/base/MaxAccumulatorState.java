package org.rtm.metrics.accumulation.base;

public class MaxAccumulatorState extends LongBinaryAccumulatorState{
	
	public MaxAccumulatorState() {
		super((x,y) -> x > y ? x : y, Long.MIN_VALUE);
	}
}
