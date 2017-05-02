package org.rtm.metrics.postprocessing;

import java.util.Map;
import java.util.concurrent.ConcurrentSkipListMap;

import org.rtm.measurement.MeasurementStatistics.AggregationType;
import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.stream.Dimension;
import org.rtm.stream.Stream;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked", "unused"})
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
			Map<String, Dimension> series = ar.getDimensionsMap();
			//logger.debug(series.toString());
			series.entrySet().stream().forEach(f -> {
				Dimension data = f.getValue();
				// not computed anymore
				//data.put(AggregationType.AVG.getShort(), data.get(AggregationType.SUM.getShort()) / data.get(AggregationType.COUNT.getShort()));
				
				// temporary, to check consistency/integrity
				long tCount = data.getHist().getTotalCount();
				long tSum = data.getHist().getTotalSum();
				data.put("hcnt", tCount);
				data.put("hsum", tSum);
				data.put("havg", tSum/tCount);
				
				computePcs(data, tCount);

			});
		});	
	}

	private void computePcs(Dimension data, long tCount) {

		long median = (long) (tCount * 0.5F);
		long eightiethPcMark = (long) (tCount * 0.8F);
		long ninetiethPcMark = (long) (tCount * 0.9F);
		long ninetyninethPcMark = (long) (tCount * 0.99F);
		
		tagValue(data, median, "pc50", tCount);
		tagValue(data, eightiethPcMark, "pc80", tCount);
		tagValue(data, ninetiethPcMark, "pc90", tCount);
		tagValue(data, ninetyninethPcMark, "pc99", tCount);
	}

	private void tagValue(Dimension data, long target, String metricName, long tCount) {
		long curCount = 0;
		CountSumBucket[] array = data.getHist().getHistogramAsArray();
		
		for(int i=0; i<array.length; i++){
			curCount += array[i].getCount();
			if(curCount >= target){
				data.put(metricName, array[i].getAvg());
				break;
			}
		}
	}

}
