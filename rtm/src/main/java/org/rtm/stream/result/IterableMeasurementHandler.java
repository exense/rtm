package org.rtm.stream.result;

import java.util.Map;
import java.util.Properties;

import org.rtm.measurement.MeasurementHelper;
import org.rtm.range.RangeBucket;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongAccumulationHelper;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public abstract class IterableMeasurementHandler {
	
	private MeasurementHelper mh;
	private Properties prop;
	
	public IterableMeasurementHandler(Properties prop){
		this.prop = prop;
		this.mh = new MeasurementHelper(this.prop);
	}
	
	public abstract LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket);
	
	protected Dimension getOrInitDimension(LongRangeValue sc, Map m) {
		String m_dimension = mh.getPrimaryDimensionValue(m);

		//TODO: get key from request props
		if(m_dimension == null || m_dimension.trim().isEmpty()){
			//TODO: get from prop/conf (default fall back key name)
			m_dimension = "groupall";
		}

		Dimension d = sc.getDimension(m_dimension);
		if(d == null){
			d = new Dimension(m_dimension);
			sc.setDimension(d);
		}
		return d;
	}

	protected void accumulateStats(Dimension d, Long value) {
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
