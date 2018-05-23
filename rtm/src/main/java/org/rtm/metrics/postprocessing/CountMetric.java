package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.base.CountAccumulator.CountAccumulatorState;

public class CountMetric implements SubscribedMetric<Long>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.CountAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		CountAccumulatorState state = (CountAccumulatorState)wobjs.get("org.rtm.metrics.accumulation.base.CountAccumulator");
		return state.getAccumulator().get();
	}

	@Override
	public String getDisplayName() {
		return "cnt";
	}

}
