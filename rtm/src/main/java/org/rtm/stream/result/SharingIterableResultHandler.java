package org.rtm.stream.result;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.MeasurementConstants;
import org.rtm.range.RangeBucket;
import org.rtm.stream.AccumulationContext;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public class SharingIterableResultHandler extends IterableResultHandler{

	private AccumulationContext sc;
	
	public SharingIterableResultHandler(Properties prop, AccumulationContext sc){
		super(prop);
		this.sc = sc;
	}

	public LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket) {
		for(Map m : iterable){
			Long accValue = (Long)m.get(MeasurementConstants.VALUE_KEY);
			Dimension d = getOrInitDimension(sc, m);
			accumulateStats(d, accValue);
		}
		
		//this handle is not supposed to be used (signature compliance)
		return sc;
	}
}
