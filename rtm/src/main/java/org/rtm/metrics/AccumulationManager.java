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
			
			WorkObject wobj = dimension.get(accumulatorName);
			if(wobj == null)
				wobj = initWorkObject(accumulator, dimension);
			
			accumulator.accumulate(wobj, value);
		}

	}

	private WorkObject initWorkObject(Accumulator accumulator, WorkDimension dimension) {
		SimpleWorkObject mwobj = new SimpleWorkObject();

		String accumulatorName = accumulator.getClass().getName();
		mwobj.setPayload(accumulator.produceFreshState());
		
		dimension.put(accumulatorName, mwobj);
		return mwobj;
	}
	
	@SuppressWarnings("unused")
	private void initWorkObjects(WorkDimension dimension) {
		accumulators.values().stream().forEach( accumulator -> {
			initWorkObject(accumulator, dimension);
		});
	}

	public void mergeAllLeft(WorkDimension dimension1, WorkDimension dimension2) {
		String name = dimension1.getDimensionName();
		if(name != dimension2.getDimensionName())
			throw new RuntimeException("Names differ. Dimension1=" + name + "; Dimension2=" + dimension2.getDimensionName());

		for(Accumulator accumulator : accumulators.values()) {
			String accumulatorName = accumulator.getClass().getName();
			WorkObject wobj1 = dimension1.get(accumulatorName);
			if(wobj1 == null)
				wobj1 = initWorkObject(accumulator, dimension1);
			accumulator.mergeLeft(wobj1, dimension2.get(accumulatorName));
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
