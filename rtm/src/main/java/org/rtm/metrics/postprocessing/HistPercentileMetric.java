package org.rtm.metrics.postprocessing;

import java.text.DecimalFormat;
import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class HistPercentileMetric implements SubscribedMetric<Long>{
	
	Float percentileMark;

	public HistPercentileMetric(Float percentileMark) {
		this.percentileMark = percentileMark/100;
	}

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.HistogramAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		Histogram state = (Histogram)wobjs.get("org.rtm.metrics.accumulation.base.HistogramAccumulator");
		return state.getValueForMark(getPercentileMark());
	}

	@Override
	public String getDisplayName() {
		DecimalFormat df = new DecimalFormat();
		df.setMaximumFractionDigits(1);
		return df.format((getPercentileMark() * 100F))+ "th pcl";
	}

	protected Float getPercentileMark() {
		return percentileMark;
	}

}
