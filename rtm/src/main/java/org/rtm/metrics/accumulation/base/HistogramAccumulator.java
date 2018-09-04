package org.rtm.metrics.accumulation.base;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.Accumulator;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class HistogramAccumulator implements Accumulator<Long, Histogram>{

	@Override
	public WorkObject buildStateObject() {
		//TODO: plug Props for dynamic hist configuration
		return new HistogramAccumulatorState(50,50);
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

	public class HistogramAccumulatorState extends Histogram implements WorkObject{

		public HistogramAccumulatorState(int nbPairs, int approxMs) {
			super(nbPairs, approxMs);
		}

	}
}
