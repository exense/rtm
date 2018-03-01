package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.LongBinaryAccumulator;

public class CountAccumulator extends LongBinaryAccumulator{
	
	@Override
	public WorkObject makeWorkObject() {
		return new LongAccumulatorWorkObject((x,y) -> x+1, 0L);
	}
}
