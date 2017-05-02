package org.rtm.metrics.postprocessing;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.rtm.measurement.MeasurementStatistics.AggregationType;
import org.rtm.stream.Stream;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class PostMetricsFilter {

	private static final Logger logger = LoggerFactory.getLogger(PostMetricsFilter.class);

	public Stream handle(Stream stream) {
		Stream copy = stream.clone();
		computePostMetrics(copy);
		return copy;
	}

	private void computePostMetrics(Stream copy) {
		ConcurrentSkipListMap<Long, AggregationResult> map = copy.getStreamData();
		map.entrySet().stream().forEach(e -> {
			AggregationResult ar = e.getValue();
			Map<String, Map> series = ar.getData();
			//logger.debug(series.toString());
			series.entrySet().stream().forEach(f -> {
				Map<String, Long> data = f.getValue();
				data.put(AggregationType.AVG.getShort(), data.get(AggregationType.SUM.getShort()) / data.get(AggregationType.COUNT.getShort()));
			});
			//
		});		
	}

}
