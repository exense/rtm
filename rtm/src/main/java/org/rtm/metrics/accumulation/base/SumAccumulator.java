package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.LongBinaryAccumulator;

public class SumAccumulator extends LongBinaryAccumulator{
	
	@Override
	public WorkObject makeWorkObject() {
		return new LongAccumulatorWorkObject((x,y) -> x+y, 0L);
	}
}
