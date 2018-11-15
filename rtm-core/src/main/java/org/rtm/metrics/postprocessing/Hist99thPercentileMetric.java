package org.rtm.metrics.postprocessing;

//TODO: make value configurable directly in conf and parse straight from classname
public class Hist99thPercentileMetric extends HistPercentileMetric{

	@Override
	protected Float getPercentileMark() {
		return 0.99F;
	}

}
