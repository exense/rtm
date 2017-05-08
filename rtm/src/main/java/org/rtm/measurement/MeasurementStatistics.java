package org.rtm.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.stream.Dimension;

public class MeasurementStatistics {

	public enum AccumulatedAggregationType {
		MIN((x,y) -> y < x ? y : x),
		MAX((x,y) -> x < y ? y : x),
		// now computed via histograms (postprocessed)
		//COUNT((x,y) -> x+y), 
		//SUM((x,y) -> x + y)
		;

		LongBinaryOperator op;

		AccumulatedAggregationType(LongBinaryOperator bop) {
			op = bop;
		}

		public LongBinaryOperator getOp() {
			return op;
		} 
	}

	public enum PostprocessedAggregationType {
		CNT, SUM, AVG,
		/* TODO: STD("std"), */ TPS, TPM,
		PCL50, PCL80, PCL90,PCL99;
	}

	public static void computeAllRegisteredPostMetrics(Dimension data, long intervalSize) {
		for(PostprocessedAggregationType t : PostprocessedAggregationType.values())
			data.put(t.toString(), computeMetric(t, data.getHist(), intervalSize));
	}

	public static Object computeMetric(PostprocessedAggregationType metric, Histogram h, long intervalSize) {
		Object value = null;

		switch(metric){
		case CNT:
			value = h.getTotalCount();
			break;
		case SUM:
			value = h.getTotalSum();
			break;
		case AVG:
			value = h.getTotalSum() / h.getTotalCount();
			break;
			/*case STD:
				//TODO: implement via running algorithm (store running mean deviation, and merge via squares) 
				break;*/
		case TPS:
			value = ((float) h.getTotalCount()) / ((float)intervalSize) * 1000F;
			break;
		case TPM:
			value = ((float)h.getTotalCount()) / ((float)intervalSize) * 60000F;
			break;
		case PCL50:
			value = getValueForMark(0.5F, h);
			break;
		case PCL80:
			value = getValueForMark(0.8F, h);
			break;
		case PCL90:
			value = getValueForMark(0.9F, h);
			break;
		case PCL99:
			value = getValueForMark(0.99F, h);
			break;
		default:
			break;
		}

		return value;
	}

	private static long getValueForMark(float pcl, Histogram histogram) {
		long curCount = 0;
		CountSumBucket[] array = histogram.getHistogramAsArray();
		long target = (long) (pcl * histogram.getTotalCount());
		for(int i=0; i<array.length; i++){
			curCount += array[i].getCount();
			if(curCount >= target){
				return array[i].getAvg();
			}
		}
		return -1;
	}

	public static List<String> getMetricList() {
		List<String> metricList = new ArrayList<>();
		for(AccumulatedAggregationType t : AccumulatedAggregationType.values())
			metricList.add(t.toString());
		for(PostprocessedAggregationType t : PostprocessedAggregationType.values())
			metricList.add(t.toString());
		return metricList;
	}
}
