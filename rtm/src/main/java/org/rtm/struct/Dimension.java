package org.rtm.struct;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

/*
 * Wraps a regular HM which represents a simple datapoint (containing multiple metrics)
 * for a given time bucket (pointed by Stream's key)
 * for a given dimension (pointed by TimeValue's key)
 */
@SuppressWarnings({"rawtypes","unchecked"})
public class Dimension extends HashMap<String, Object>{
	private static final long serialVersionUID = 5989391368060961616L;

	private Map<String, Map<String, LongAccumulator>> accumulationHelper;

	public Dimension(){
		super();
		this.accumulationHelper = new HashMap<>();
	}

	public Map<String, Map<String, LongAccumulator>> getAccumulationHelper() {
		return accumulationHelper;
	}

	public void setAccumulationHelper(Map<String, Map<String, LongAccumulator>> accumulationHelper) {
		this.accumulationHelper = accumulationHelper;
	}

	public void copyAndFlush() {
		accumulationHelper.entrySet().stream().forEach(
				x -> {
					String dimension = x.getKey();
					super.put(dimension, new HashMap<>());
					x.getValue().entrySet().stream().forEach(
							y -> {
								Map metrics = (Map)super.get(dimension);
								metrics.put(y.getKey(), y.getValue().longValue());
							}
							);
				}
				);		
	}

}
