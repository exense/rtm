package org.rtm.metrics.accumulation;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.MeasurementConstants;
import org.rtm.range.RangeBucket;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public class MergingAccumulator extends MeasurementAccumulator{
	
	public MergingAccumulator(Properties prop){
		super(prop);
	}

	public LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket) {

		LongRangeValue tv = new LongRangeValue(myBucket.getLowerBound());
		
		for(Map m : iterable){
			Long accValue = (Long)m.get(MeasurementConstants.VALUE_KEY);
			Dimension d = getOrInitDimension(tv, m);
			accumulateStats(d, accValue);
		}

		flushAccDataToValueLocal(tv);

		return tv;
	}
	
	private void flushAccDataToValueLocal(LongRangeValue tv) {
		tv.values().stream().forEach(v -> v.copyAndFlush());
	}

}
