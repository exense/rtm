package org.rtm.metrics.accumulation.base;

import java.util.Properties;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.Accumulator;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.utils.ServiceUtils;

public class HistogramAccumulator implements Accumulator<Long, Histogram>{

	private int nbPairs;
	private Properties props;
	
	@Override
	public void initAccumulator(Properties props) {
		this.props = props;
		nbPairs = Integer.parseInt((String)ServiceUtils.decideServiceProperty(props, "aggregateService.histSize", 40));
	}

	@Override
	public WorkObject buildStateObject(String dimensionName) {
		String value = (String) ServiceUtils.decideServiceProperty(props, "aggregateService.histApp"+"."+dimensionName, null);
		int dimensionApproxMs = (value == null) ? Integer.parseInt((String)ServiceUtils.decideServiceProperty(props, "aggregateService.histApp", 200)) : Integer.parseInt(value);
		return new HistogramAccumulatorState(nbPairs, dimensionApproxMs);
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