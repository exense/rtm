package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.accumulation.AccumulatorState;

public class CountAccumulator extends LongBinaryAccumulator{
	
	@Override
	protected String getConcreteAccumulatorKey() {
		return this.getClass().getName();
	}
	
	@Override
	public Long mergeValues(Long value1, Long value2) {
		return value1 + value2;
	}
	
	@Override
	public AccumulatorState produceFreshState() {
		return new CountAccumulatorState();
	}
	
	public class CountAccumulatorState extends LongBinaryAccumulatorState{
		
		public CountAccumulatorState() {
			super((x,y) -> x+1, 0L);
		}
	}


}
