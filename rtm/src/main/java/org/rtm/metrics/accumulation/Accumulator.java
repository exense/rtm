package org.rtm.metrics.accumulation;

import org.rtm.metrics.WorkObject;

/*
 * An interface for stateless accumulators.
 * State is kept in their WorkObject.
 * 
 */
public interface Accumulator<T,V> {
	
	public AccumulatorState buildStateObject();
	
	public void accumulate(WorkObject wobj, V value);
	
	public void mergeLeft(WorkObject wobj1, WorkObject wobj2);
	
	public V getValue(WorkObject wobj);
	
}