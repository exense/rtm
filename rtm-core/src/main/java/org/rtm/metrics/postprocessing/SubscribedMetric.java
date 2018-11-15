package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;

public interface SubscribedMetric<T> {
	
	public String[] getSubscribedAccumulatorsList();
	
	public T computeMetric(Map<String, WorkObject> wobjs, Long intervalSize);
	
	public String getDisplayName();

}
