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
import org.rtm.stream.result.FinalAggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
public class MetricsManager {

	private static final Logger logger = LoggerFactory.getLogger(MetricsManager.class);

	private Properties props;

	public MetricsManager(Properties props) {
		this.props = props;
	}

	public Stream handle(Stream stream) throws Exception {
		return computePostMetrics(stream);
	}

	private Stream computePostMetrics(Stream stream) throws Exception {
		Stream resStream = new Stream(props);
		
		Map<Long, AggregationResult> originMap = stream.getStreamData();
		Map<Long, AggregationResult> resultMap = new ConcurrentSkipListMap<Long, AggregationResult>();

		resStream.setStreamData(resultMap);
		
		if(originMap == null || originMap.size() < 1)
			return resStream;
			
		Long intervalSize = Long.parseLong(stream.getStreamProp().getProperty(Stream.INTERVAL_SIZE_KEY));

		if(intervalSize == null)
			throw new Exception("Null interval size.");

		MeasurementStatistics stats = new MeasurementStatistics(stream.getStreamProp());

		originMap.entrySet().stream().forEach(e -> {
			AggregationResult ar = e.getValue();
			AggregationResult finalAr = new FinalAggregationResult<Long>(ar.getStreamPayloadIdentifier());
			Map<String, WorkDimension> series = ar.getDimensionsMap();

			Map<String, Dimension> result = new HashMap<String, Dimension>();
			series.entrySet().stream().forEach(f -> {
				WorkDimension data = f.getValue();
				result.put(f.getKey(), stats.computeAllRegisteredPostMetrics(data, intervalSize));
			});

			finalAr.setDimensionsMap(result);
			resultMap.put(e.getKey(), finalAr);
		});	
		
		return resStream;
	}
}
