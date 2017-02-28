package org.rtm.measurement;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.MeasurementConstants;
import org.rtm.range.RangeBucket;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public class SharingAccumulator extends MeasurementAccumulator{

	private AccumulationContext sc;
	
	public SharingAccumulator(Properties prop, RangeBucket<Long> myBucket){
		super(prop);
		sc = new AccumulationContext(myBucket);
	}

	public AccumulationContext getAccumulationContext() {
		return sc;
	}

	public LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket) {
		
		for(Map m : iterable){
			//TODO: get value key from prop
			Long accValue = (Long)m.get(MeasurementConstants.VALUE_KEY);
			Dimension d = getOrInitDimension(this.sc, m);
			accumulateStats(d, accValue);
		}
		
		//this handle is not supposed to be used (signature compliance)
		return this.sc;
	}
	
	public String toString(){
		return sc.getStreamPayloadIdentifier().getIdAsTypedObject().toString();
	}
}
