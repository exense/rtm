package org.rtm.metrics.postprocessing;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

import org.rtm.measurement.MeasurementStatistics;
import org.rtm.stream.Dimension;
import org.rtm.stream.Stream;
import org.rtm.stream.WorkDimension;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class MetricsManager {

	private static final Logger logger = LoggerFactory.getLogger(MetricsManager.class);

	private Properties props;
	
	public MetricsManager(Properties props) {
		this.props = props;
	}
	
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
		
		MeasurementStatistics stats = new MeasurementStatistics(copy.getStreamProp());
		
		map.entrySet().stream().forEach(e -> {
			AggregationResult ar = e.getValue();
			Map<String, WorkDimension> series = ar.getDimensionsMap();
			
			Map<String, Dimension> result = new HashMap<String, Dimension>();
			series.entrySet().stream().forEach(f -> {
				WorkDimension data = f.getValue();
				result.put(f.getKey(), stats.computeAllRegisteredPostMetrics(data, intervalSize));
			});
			
			ar.setDimensionsMap(result);
		});	
	}
}