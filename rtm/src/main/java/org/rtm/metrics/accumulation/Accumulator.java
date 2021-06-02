package org.rtm.metrics.accumulation;

import java.util.Properties;

import ch.exense.commons.app.Configuration;
import org.rtm.metrics.WorkObject;

/*
 * An interface for stateless accumulators.
 * State is kept in their WorkObject.
 * 
 */
public interface Accumulator<V,T> {
	
	public void initAccumulator(Properties props, Configuration configuration);
	
	public WorkObject buildStateObject(String dimensionName);
	
	public void accumulate(WorkObject wobj, V value);
	
	public void mergeLeft(WorkObject wobj1, WorkObject wobj2);
	
	public T getValue(WorkObject wobj);
	
}