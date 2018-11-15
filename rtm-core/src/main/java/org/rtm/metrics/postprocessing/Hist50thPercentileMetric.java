package org.rtm.metrics.postprocessing;

//TODO: make value configurable directly in conf and parse straight from classname
public class Hist50thPercentileMetric extends HistPercentileMetric{

	@Override
	protected Float getPercentileMark() {
		return 0.50F;
	}

}
