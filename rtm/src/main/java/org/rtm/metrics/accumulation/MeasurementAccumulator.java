package org.rtm.metrics.accumulation;

import java.util.Map;
import java.util.Properties;

import org.rtm.measurement.MeasurementHelper;
import org.rtm.measurement.MeasurementStatistics.AccumulatedAggregationType;
import org.rtm.range.RangeBucket;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

@SuppressWarnings("rawtypes")
public abstract class MeasurementAccumulator {
	
	private MeasurementHelper mh;
	private Properties prop;
	
	public MeasurementAccumulator(Properties prop){
		this.prop = prop;
		this.mh = new MeasurementHelper(this.prop);
	}
	
	public abstract LongRangeValue handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket);
	
	protected Dimension getOrInitDimension(LongRangeValue sc, Map m) {
		String m_dimension = mh.getPrimaryDimensionValue(m);

		if(m_dimension == null || m_dimension.trim().isEmpty()){
			m_dimension = "default";
		}

		Dimension d = sc.getDimension(m_dimension);
		if(d == null){
			d = new Dimension(m_dimension, Integer.parseInt(prop.getProperty("histogram.nbPairs")), Integer.parseInt(prop.getProperty("histogram.approxMs")));
			sc.setDimension(d);
		}
		return d;
	}

	protected void accumulateStats(Dimension d, Long value) {
		LongAccumulationHelper la = d.getAccumulationHelper();
		String metric = null;
		/*// now done via histogram
		metric = AggregationType.COUNT.toString();
		if(la.isInit(metric))
			la.initializeAccumulatorForMetric(metric, (x,y) -> x+1, 0L);
		la.accumulateMetric(metric, 1);
		
		metric = AggregationType.SUM.toString();
		if(la.isInit(metric))
			la.initializeAccumulatorForMetric(metric, (x,y) -> x+y, 0L);
		la.accumulateMetric(metric, value);
		*/
		
		metric = AccumulatedAggregationType.MIN.toString();
		if(la.isInit(metric))
			la.initializeAccumulatorForMetric(metric, (x,y) -> x < y ? x : y, Long.MAX_VALUE);
		la.accumulateMetric(metric, value);
		
		metric = AccumulatedAggregationType.MAX.toString();
		if(la.isInit(metric))
			la.initializeAccumulatorForMetric(metric, (x,y) -> x > y ? x : y, Long.MIN_VALUE);
		la.accumulateMetric(metric, value);
		
		d.getHist().ingest(value);
	}

}
