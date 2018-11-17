package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.WorkObject;

public class MaxAccumulator extends LongBinaryAccumulator{

	public MaxAccumulator(){
		super();
	}
	
	@Override
	protected String getConcreteAccumulatorKey() {
		return this.getClass().getName();
	}
	
	@Override
	public Long mergeValues(Long value1, Long value2) {
		return value1 > value2 ? value1 : value2;
	}
	
	@Override
	public WorkObject buildStateObject() {
		return new MaxAccumulatorState();
	}
}
