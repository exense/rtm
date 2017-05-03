package org.rtm.metrics.postprocessing;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.rtm.measurement.MeasurementStatistics;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.PayloadIdentifier;
import org.rtm.stream.Stream;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class PostMetricsFilter {

	private static final Logger logger = LoggerFactory.getLogger(PostMetricsFilter.class);

	public Stream handle(Stream stream) {
		Map streamData = stream.getStreamData();
		if(streamData != null && streamData.size() > 0){
			Stream copy = stream.clone();
			computePostMetrics(copy);
			return copy;
		}
		return stream;
	}

	private void computePostMetrics(Stream copy) {
		ConcurrentSkipListMap<Long, AggregationResult> map = copy.getStreamData();

		Long intervalSize = Long.parseLong(copy.getStreamProp().getProperty(Stream.INTERVAL_SIZE_KEY));

		if(intervalSize == null)
			return;
		
		map.entrySet().stream().forEach(e -> {
			AggregationResult ar = e.getValue();
			Map<String, Dimension> series = ar.getDimensionsMap();
			series.entrySet().stream().forEach(f -> {
				Dimension data = f.getValue();
				MeasurementStatistics.computeAllRegisteredPostMetrics(data, intervalSize);
			});
		});	
	}

	//TODO: use RangeBucket or extend identifier to provide duration value
	/*
	private Long getIntervalSize(Map<Long, AggregationResult> map) {
		TimeBasedPayloadIdentifier first = null;
		for(AggregationResult ar : map.values()){
			if(ar != null){
				PayloadIdentifier id = ((LongRangeValue) ar).getStreamPayloadIdentifier();
				if(id != null){
					first = (TimeBasedPayloadIdentifier) id;
					break;
				}
			}
		}
		if(first == null){
			return null;
		}else{
			return ((long)first.getUpperBound()) - ((long)first.getLowerBound());
		}
	}*/


}
