package org.rtm.metrics.accumulation.base;

public class MinAccumulatorState extends LongBinaryAccumulatorState{
	
	public MinAccumulatorState() {
		super((x,y) -> x < y ? x : y, Long.MAX_VALUE);
	}
}
