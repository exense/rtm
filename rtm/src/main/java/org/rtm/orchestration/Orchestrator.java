package org.rtm.orchestration;

public abstract class Orchestrator {
	
	public long computeOptimalInterval(long timeWindow, int targetSeriesDots){
		long result = Math.abs(timeWindow / targetSeriesDots);
		return result;
	}

}
