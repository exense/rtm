package org.rtm.metrics;

public interface AbstractMetric {
	
	public String[] subscribedAccumulators();
	
	public Object postProcess();
}
