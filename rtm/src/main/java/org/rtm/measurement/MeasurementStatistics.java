package org.rtm.measurement;

import java.util.ArrayList;
import java.util.List;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.WorkObject;
import org.rtm.metrics.accumulation.base.LongBinaryAccumulator;
import org.rtm.metrics.accumulation.base.CountAccumulator.CountAccumulatorState;
import org.rtm.metrics.accumulation.base.SumAccumulator.SumAccumulatorState;
import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.metrics.accumulation.histograms.Histogram;
import org.rtm.stream.FinalDimension;
import org.rtm.stream.WorkDimension;

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

	public static FinalDimension computeAllRegisteredPostMetrics(WorkDimension data, long intervalSize) {
		
		FinalDimension res = new FinalDimension(data.getDimensionName());
		
		for(PostprocessedAggregationType t : PostprocessedAggregationType.values())
			res.put(t.toString(), computeMetric(t, data, intervalSize));
		
		return res;
	}

	public static Object computeMetric(PostprocessedAggregationType metric, WorkDimension data, long intervalSize) {
		Object value = 0L;
		
		WorkObject countWobj = data.get("org.rtm.metrics.accumulation.base.CountAccumulator");
		CountAccumulatorState count = (CountAccumulatorState)countWobj.getPayload(LongBinaryAccumulator.ACCUMULATOR_KEY);
		Long countValue = count.getAccumulator().get();
		
		WorkObject sumWobj = data.get("org.rtm.metrics.accumulation.base.SumAccumulator");
		SumAccumulatorState sum = (SumAccumulatorState)sumWobj.getPayload(LongBinaryAccumulator.ACCUMULATOR_KEY);
		Long sumValue = sum.getAccumulator().get();
		
		Float avgValue = 0F;
		try{
			avgValue = (float)sumValue / countValue; 
		}catch(ArithmeticException e){
			avgValue = 0F;
		}
		
		switch(metric){
		case CNT:
			return countValue;
		case SUM:
			return sumValue;
		case AVG:
			return avgValue;
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
