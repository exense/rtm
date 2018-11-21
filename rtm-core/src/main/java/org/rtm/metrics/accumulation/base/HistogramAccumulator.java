package org.rtm.metrics.accumulation.base;

import java.util.Properties;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.Accumulator;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.utils.ServiceUtils;

public class HistogramAccumulator implements Accumulator<Long, Histogram>{

	private int nbPairs;
	private int approxMs;
	
	@Override
	public void initAccumulator(Properties props) {
		nbPairs = Integer.parseInt((String)ServiceUtils.decideServiceProperty(props, "aggregateService.histSize", "40"));
		approxMs = Integer.parseInt((String)ServiceUtils.decideServiceProperty(props, "aggregateService.histApp", "200"));
	}

	@Override
	public WorkObject buildStateObject() {
		return new HistogramAccumulatorState(this.nbPairs, this.approxMs);
	}

	@Override
	public void accumulate(WorkObject wobj, Long value) {
		((HistogramAccumulatorState) wobj).ingest(value);
	}

	@Override
	public void mergeLeft(WorkObject wobj1, WorkObject wobj2) {
		try {
			((HistogramAccumulatorState) wobj1).merge(((HistogramAccumulatorState)wobj2));
		} catch (Exception e) {
			e.printStackTrace();
			throw new RuntimeException("Couldn't merge histograms.");
		}
	}

	@Override
	public Histogram getValue(WorkObject wobj) {
		return (Histogram) wobj;
	}

}
