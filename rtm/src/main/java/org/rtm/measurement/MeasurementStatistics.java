package org.rtm.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.stream.Dimension;

public class MeasurementStatistics {

		public enum AccumulatedAggregationType {
			//COUNT((x,y) -> x+y), 
			MIN((x,y) -> y < x ? y : x),
			MAX((x,y) -> x < y ? y : x),
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
				computeMetric(data, t, intervalSize);
		}

		public static void computeMetric(Dimension data, PostprocessedAggregationType metric, long intervalSize) {
			switch(metric){
			case CNT:
				data.put(PostprocessedAggregationType.CNT.toString(), data.getHist().getTotalCount());
				break;
			case SUM:
				data.put(PostprocessedAggregationType.SUM.toString(), data.getHist().getTotalSum());
				break;
			case AVG:
				data.put(PostprocessedAggregationType.AVG.toString(), data.getHist().getTotalSum() / data.getHist().getTotalCount());
				break;
			/*case STD:
				//TODO: implement via running algorithm (store running mean deviation, and merge via squares) 
				break;*/
			case TPS:
				data.put(PostprocessedAggregationType.TPS.toString(), ((float)data.getHist().getTotalCount() / (float)intervalSize) * 1000F);
				break;
			case TPM:
				data.put(PostprocessedAggregationType.TPM.toString(), ((float)data.getHist().getTotalCount() / (float)intervalSize) * 60000F);
				break;
			case PCL50:
				data.put(PostprocessedAggregationType.PCL50.toString(), getValueForMark(0.5F, data.getHist()));
				break;
			case PCL80:
				data.put(PostprocessedAggregationType.PCL80.toString(), getValueForMark(0.8F, data.getHist()));
				break;
			case PCL90:
				data.put(PostprocessedAggregationType.PCL90.toString(), getValueForMark(0.9F, data.getHist()));
				break;
			case PCL99:
				data.put(PostprocessedAggregationType.PCL99.toString(), getValueForMark(0.99F, data.getHist()));
				break;
			default:
			}
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
