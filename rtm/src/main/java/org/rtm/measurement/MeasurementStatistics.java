package org.rtm.measurement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.base.CountAccumulator.CountAccumulatorState;
import org.rtm.metrics.accumulation.base.LongBinaryAccumulator;
import org.rtm.metrics.accumulation.base.SumAccumulator.SumAccumulatorState;
import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.metrics.postprocessing.SubscribedMetric;
import org.rtm.stream.FinalDimension;
import org.rtm.stream.WorkDimension;

@SuppressWarnings({"rawtypes","unchecked"})
public class MeasurementStatistics {

	private Properties props;
	
	private String[] registeredMetrics;
	private Map<String,SubscribedMetric> subscribedMetrics;
	
	public MeasurementStatistics(Properties props) {
		this.props = props;

		registeredMetrics = props.getProperty("aggregateService.registeredMetrics").split(",");
		subscribedMetrics = new HashMap<String,SubscribedMetric>();
		
		for(String subscribedMetric : registeredMetrics) {
			Class<?> clazz;
			try {
				clazz = Class.forName(subscribedMetric);
				SubscribedMetric subscribedMetricObj = (SubscribedMetric)clazz.getConstructor().newInstance();
				subscribedMetrics.put(subscribedMetric, subscribedMetricObj);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public FinalDimension computeAllRegisteredPostMetrics(WorkDimension data, long intervalSize) {
		
		FinalDimension res = new FinalDimension(data.getDimensionName());

		subscribedMetrics.values().stream().forEach(v ->{
			res.put(v.getDisplayName(), v.computeMetric((Map)data, intervalSize));
		});
			
		return res;
	}

	public List<String> getMetricList() {
		List<String> metricList = new ArrayList<>();
		
		subscribedMetrics.values().stream().forEach(v ->{
			metricList.add(v.getDisplayName());
		});
		return metricList;
	}

}
