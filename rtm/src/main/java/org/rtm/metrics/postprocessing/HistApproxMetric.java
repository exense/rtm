package org.rtm.metrics.postprocessing;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.histograms.Histogram;

import java.util.Map;

public class HistApproxMetric implements SubscribedMetric<Long>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.HistogramAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		Histogram state = (Histogram)wobjs.get("org.rtm.metrics.accumulation.base.HistogramAccumulator");
		return Long.valueOf(state.getApproxMs());
	}

	@Override
	public String getDisplayName() {
		return "pcl precision";
	}

}
