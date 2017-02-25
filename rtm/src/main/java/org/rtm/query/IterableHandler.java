package org.rtm.query;

import java.util.Map;
import java.util.Properties;

import org.rtm.stream.Dimension;
import org.rtm.stream.LongAccumulationHelper;
import org.rtm.stream.TimeValue;
import org.rtm.time.RangeBucket;

@SuppressWarnings("rawtypes")
public class IterableHandler{

	public TimeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket, Properties prop, AccumulationContext sc) {

		TimeValue tv = new TimeValue(myBucket);
		MeasurementHelper mh = new MeasurementHelper(prop);

		for(Map m : iterable){

			String m_dimension = mh.getPrimaryDimensionValue(prop, m);
			
			if(m_dimension == null || m_dimension.isEmpty()){
				// default fall back TODO: get from prop/conf
				m_dimension = "groupall";
			}
			
			Dimension d = tv.getDimension(m_dimension);
			if(d == null){
				d = new Dimension(m_dimension);
				tv.setDimension(d);
			}
			
			accumulateStats(d);
		}
		
		tv.values().stream().forEach(v -> v.copyAndFlush());
		
		return tv;
	}

	private void accumulateStats(Dimension d) {
		LongAccumulationHelper la = d.getAccumulationHelper();
		//TODO: we'll only do hardcoded counts for now
		if(la.isInit("count"))
			la.initializeAccumulatorForMetric("count", (x,y) -> x+1, 0L);
		la.accumulateMetric("count", 1);	}

}
