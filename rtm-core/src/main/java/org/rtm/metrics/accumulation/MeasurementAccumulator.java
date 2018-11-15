package org.rtm.metrics.accumulation;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.MeasurementConstants;
import org.rtm.measurement.MeasurementHelper;
import org.rtm.metrics.AccumulationManager;
import org.rtm.stream.WorkDimension;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public class MeasurementAccumulator {
	
	private MeasurementHelper mh;
	private AccumulationManager amgr;
	
	public MeasurementAccumulator(Properties prop){
		this.mh = new MeasurementHelper(prop);
		this.amgr = new AccumulationManager(prop);
	}
	
	public void handle(LongRangeValue lrv, Iterable<? extends Map> iterable) {
		for(Map m : iterable)
			amgr.accumulateAll(getOrInitDimension(lrv, m), m.get(MeasurementConstants.VALUE_KEY));			
	}
	
	protected WorkDimension getOrInitDimension(LongRangeValue lrv, Map m) {
		String m_dimension = mh.getPrimaryDimensionValue(m);

		if(m_dimension == null || m_dimension.trim().isEmpty()){
			m_dimension = "default";
		}

		WorkDimension dimension = (WorkDimension)lrv.getDimension(m_dimension);
		if(dimension == null){
			dimension = new WorkDimension(m_dimension);
			lrv.setDimension(dimension);
		}
		return dimension;
	}
	

	public void mergeDimensionsLeft(WorkDimension dimension1, WorkDimension dimension2) {
		amgr.mergeAllLeft(dimension1, dimension2);
	}

}
