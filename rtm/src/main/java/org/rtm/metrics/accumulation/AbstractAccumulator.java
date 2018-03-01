package org.rtm.metrics.accumulation;

import org.rtm.metrics.WorkObject;
import org.rtm.range.RangeBucket;

public interface AbstractAccumulator<T,V> {
	
	public void initialize(WorkObject wobj);
	
	public void accumulate(RangeBucket<T> bucket, V value);

	public V getValue();
	
	public WorkObject makeWorkObject();
}