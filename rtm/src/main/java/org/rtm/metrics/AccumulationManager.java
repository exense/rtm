package org.rtm.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.rtm.metrics.accumulation.Accumulator;
import org.rtm.stream.WorkDimension;

@SuppressWarnings({"rawtypes","unchecked"})
public class AccumulationManager {

	private String[] accumulatorRegistry;

	private Map<String,Accumulator> accumulators;

	@SuppressWarnings("unused")
	private AccumulationManager() {}

	public AccumulationManager(Properties rtmProps){

		accumulatorRegistry = rtmProps.getProperty("aggregateService.registeredAccumulators").split(",");
		accumulators = new HashMap<String,Accumulator>();

		for(String entry : accumulatorRegistry) {
			Class<?> clazz;
			try {
				clazz = Class.forName(entry);
				Accumulator accumulator = (Accumulator)clazz.getConstructor().newInstance();
				accumulators.put(entry, accumulator);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public void accumulateAll(WorkDimension dimension, Object value) {
		for( Accumulator accumulator : accumulators.values())
		{
			String accumulatorName = accumulator.getClass().getName();
			
			if(dimension.get(accumulatorName) == null)
				initWorkObject(accumulator, dimension);
			
			accumulator.accumulate(dimension.get(accumulatorName), value);
		}

	}

	private void initWorkObject(Accumulator accumulator, WorkDimension dimension) {
		String accumulatorName = accumulator.getClass().getName();
		dimension.put(accumulatorName, accumulator.buildStateObject());
	}
	
	@SuppressWarnings("unused")
	private void initWorkObjects(WorkDimension dimension) {
		accumulators.values().stream().forEach( accumulator -> {
			initWorkObject(accumulator, dimension);
		});
	}

	public void mergeAllLeft(WorkDimension dimension1, WorkDimension dimension2) {
		String name = dimension1.getDimensionName();
		if(!name.equals(dimension2.getDimensionName()))
			throw new RuntimeException("Names differ. Dimension1=" + name + "; Dimension2=" + dimension2.getDimensionName());

		for(Accumulator accumulator : accumulators.values()) {
			String accumulatorName = accumulator.getClass().getName();

			if(dimension1.get(accumulatorName) == null)
				initWorkObject(accumulator, dimension1);
			
			accumulator.mergeLeft(dimension1.get(accumulatorName), dimension2.get(accumulatorName));
		}
	}

	public Object getValueForAccumulator(WorkDimension dimension, String accumulatorClassName) {
		Accumulator accumulator = accumulators.get(accumulatorClassName);
		return accumulator.getValue(dimension.get(accumulator.getClass().getName()));
	}

	public Map<String, Object> getAllValues(WorkDimension dimension) {
		Map<String, Object> allValues = new HashMap<String, Object>();
		
		accumulators.entrySet().stream().forEach( accumulatorEntry -> {
			
			String accumulatorKey = accumulatorEntry.getKey();
			WorkObject wobj = dimension.get(accumulatorKey);
			Object value = accumulatorEntry.getValue().getValue(wobj);
			allValues.put(accumulatorEntry.getKey(), value);
			
		});
		
		return allValues;
	}
}
