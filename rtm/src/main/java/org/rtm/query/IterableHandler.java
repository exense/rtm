package org.rtm.query;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.MeasurementConstants;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongAccumulationHelper;
import org.rtm.stream.LongRangeValue;
import org.rtm.time.RangeBucket;

@SuppressWarnings("rawtypes")
public class IterableHandler{

	//TODO: use AccumulationContext for more efficient mem mgmt (LongAcc's)
	// or just pass the global time value to access Helpers via Dimension directly?
	public LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket, Properties prop, AccumulationContext sc) {

		LongRangeValue tv = new LongRangeValue(myBucket);
		MeasurementHelper mh = new MeasurementHelper(prop);

		for(Map m : iterable){

			String m_dimension = mh.getPrimaryDimensionValue(prop, m);
			//TODO: get key from request props
			Long accValue = (Long)m.get(MeasurementConstants.VALUE_KEY);

			if(m_dimension == null || m_dimension.isEmpty()){
				// default fall back TODO: get from prop/conf
				m_dimension = "groupall";
			}

			Dimension d = tv.getDimension(m_dimension);
			if(d == null){
				d = new Dimension(m_dimension);
				tv.setDimension(d);
			}

			accumulateStats(d, accValue);
		}

		tv.values().stream().forEach(v -> v.copyAndFlush());

		return tv;
	}

	private void accumulateStats(Dimension d, Long value) {
		LongAccumulationHelper la = d.getAccumulationHelper();
		//TODO: we'll only do hardcoded counts for now
		if(la.isInit("count"))
			la.initializeAccumulatorForMetric("count", (x,y) -> x+1, 0L);
		la.accumulateMetric("count", 1);
		
		if(la.isInit("sum"))
			la.initializeAccumulatorForMetric("sum", (x,y) -> x+y, 0L);
		la.accumulateMetric("sum", value);
	}

}
