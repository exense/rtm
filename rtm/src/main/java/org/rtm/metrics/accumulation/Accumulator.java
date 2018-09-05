package org.rtm.metrics.accumulation;

import org.rtm.metrics.WorkObject;

/*
 * An interface for stateless accumulators.
 * State is kept in their WorkObject.
 * 
 */
public interface Accumulator<V,T> {
	
	public WorkObject buildStateObject();
	
	public void accumulate(WorkObject wobj, V value);
	
	public void mergeLeft(WorkObject wobj1, WorkObject wobj2);
	
	public T getValue(WorkObject wobj);
	
}