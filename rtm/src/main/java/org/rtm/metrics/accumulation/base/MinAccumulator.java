package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.accumulation.AccumulatorState;

public class MinAccumulator extends LongBinaryAccumulator{
	
	@Override
	protected String getConcreteAccumulatorKey() {
		return this.getClass().getName();
	}
	
	@Override
	public Long mergeValues(Long value1, Long value2) {
		return value1 < value2 ? value1 : value2;
	}
	
	@Override
	public AccumulatorState produceFreshState() {
		return new MinAccumulatorState();
	}
	
	public class MinAccumulatorState extends LongBinaryAccumulatorState{
		
		public MinAccumulatorState() {
			super((x,y) -> x < y ? x : y, Long.MAX_VALUE);
		}
	}
}
