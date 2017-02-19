package org.rtm.queries;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

import org.rtm.buckets.RangeBucket;
import org.rtm.commons.MeasurementConstants;
import org.rtm.core.MeasurementAggregator.AggregationType;
import org.rtm.struct.Dimension;

@SuppressWarnings("rawtypes")
public class CursorHandler{

	public Dimension handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket) {

		Dimension d = new Dimension();
		Map<String, Map<String, LongAccumulator>> acc = d.getAccumulationHelper();

		for(Map m : iterable){

			String dimensionName = (String) m.get(MeasurementConstants.NAME_KEY);
			Map<String, LongAccumulator> dimension = acc.get(dimensionName);
			if(dimensionName == null)
				acc.put(dimensionName, new HashMap<>());

			// we'll only do counts for now
			LongAccumulator count = dimension.get(AggregationType.COUNT.getShort());
			if(count == null)
				count = new LongAccumulator((x,y) -> x+1 , 0);
			count.accumulate(1);
		}
		
		d.copyAndFlush();
		
		return d;
	}

}
