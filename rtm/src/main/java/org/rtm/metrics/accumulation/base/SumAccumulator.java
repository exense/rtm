package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.accumulation.AccumulatorState;
import org.rtm.metrics.accumulation.LongBinaryAccumulator;

public class SumAccumulator extends LongBinaryAccumulator{
	
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
		return new SumAccumulatorState();
	}

	public class SumAccumulatorState extends LongBinaryAccumulatorState{
		
		public SumAccumulatorState() {
			super((x,y) -> x+y, 0L);
		}
	}
}
