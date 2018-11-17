package org.rtm.metrics.postprocessing;

import java.util.Map;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.base.MaxAccumulatorState;

public class MaxMetric implements SubscribedMetric<Long>{

	@Override
	public String[] getSubscribedAccumulatorsList() {
		return new String[] {"org.rtm.metrics.accumulation.base.MaxAccumulator"};
	}

	@Override
	public Long computeMetric(Map<String, WorkObject> wobjs, Long intervalSize) {
		MaxAccumulatorState state = (MaxAccumulatorState)wobjs.get("org.rtm.metrics.accumulation.base.MaxAccumulator");
		return state.getAccumulator().get();
	}

	@Override
	public String getDisplayName() {
		return "max";
	}

}
