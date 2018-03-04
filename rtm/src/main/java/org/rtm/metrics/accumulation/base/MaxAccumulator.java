package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.accumulation.AccumulatorState;

public class MaxAccumulator extends LongBinaryAccumulator{

	@Override
	protected String getConcreteAccumulatorKey() {
		return this.getClass().getName();
	}
	
	@Override
	public Long mergeValues(Long value1, Long value2) {
		return value1 > value2 ? value1 : value2;
	}
	
	@Override
	public AccumulatorState buildStateObject() {
		return new MaxAccumulatorState();
	}
	
	public class MaxAccumulatorState extends LongBinaryAccumulatorState{
		
		public MaxAccumulatorState() {
			super((x,y) -> x > y ? x : y, Long.MIN_VALUE);
		}
	}
}
