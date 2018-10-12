package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class HistTPMMetric implements SubscribedMetric<Float>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.HistogramAccumulator"};
	}

	@Override
	public Float computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		Histogram state = (Histogram)wobjs.get("org.rtm.metrics.accumulation.base.HistogramAccumulator");
		return ((float)state.getTotalCount()) / ((float)intervalSize) * 60000F;
	}

	@Override
	public String getDisplayName() {
		return "tpm";
	}

}
