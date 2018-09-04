package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.base.MinAccumulator.MinAccumulatorState;

public class MinMetric implements SubscribedMetric<Long>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.MinAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		MinAccumulatorState state = (MinAccumulatorState)wobjs.get("org.rtm.metrics.accumulation.base.MinAccumulator");
		return state.getAccumulator().get();
	}

	@Override
	public String getDisplayName() {
		return "min";
	}

}
