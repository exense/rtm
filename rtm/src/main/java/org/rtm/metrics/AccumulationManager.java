package org.rtm.metrics;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.AbstractAccumulator;
import org.rtm.metrics.accumulation.base.SumAccumulator;
import org.rtm.range.RangeBucket;

@SuppressWarnings({"rawtypes","unchecked"})
public class AccumulationManager {
	
	private String[] accumulatorRegistry;
	
	private Map<String,AbstractAccumulator> accumulators;

	@SuppressWarnings("unused")
	private AccumulationManager() {}
	
	public AccumulationManager(Properties rtmProps) throws Exception {
		
		accumulatorRegistry = rtmProps.getProperty("aggregateService.registeredAccumulators").split(",");
		accumulators = new HashMap<String,AbstractAccumulator>();
		
		for(String entry : accumulatorRegistry) {
			Class<?> clazz = Class.forName(entry);
			AbstractAccumulator accumulator = (AbstractAccumulator)clazz.getConstructor().newInstance();
			accumulator.initialize(accumulator.makeWorkObject());
			accumulators.put(entry, accumulator);
		}
		
	}
	
	public void accumulate(RangeBucket bucket, Object value) {
		for(AbstractAccumulator accumulator : accumulators.values())
			accumulator.accumulate(bucket, value);
	}

	public Object getValueForAccumulator(String accumulatorClassName) {
		return accumulators.get(accumulatorClassName).getValue();
	}
	
	public Map<String, Object> getAllValues() {
		Map<String, Object> allValues = new HashMap<String, Object>();
		for(Entry<String, AbstractAccumulator> accumulator : accumulators.entrySet())
			allValues.put(accumulator.getKey(), accumulator.getValue().getValue());
		return allValues;
	}
}
