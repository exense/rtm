package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class HistAverageMetric implements SubscribedMetric<Long>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.HistogramAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		Histogram state = (Histogram)wobjs.get("org.rtm.metrics.accumulation.base.HistogramAccumulator");
		return state.getTotalSum() / state.getTotalCount();
	}

	@Override
	public String getDisplayName() {
		return "avg";
	}

}
