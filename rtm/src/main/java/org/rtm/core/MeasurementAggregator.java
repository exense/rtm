package org.rtm.core;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math3.stat.descriptive.moment.StandardDeviation;
import org.apache.commons.math3.stat.descriptive.rank.Percentile;
import org.rtm.exception.ShouldntHappenException;

public class MeasurementAggregator {

	public enum AggregationType {
		COUNT("cnt"), MIN("min"), MAX("max"), SUM("sum"), AVG("avg"), STD("std"), TPS("tps"), TPM("tpm"),
		PCL50("pcl50"), PCL80("pcl80"), PCL90("pcl90"), PCL99("pcl99"); 

		String shortName;
		AggregationType(String s) {
			shortName = s;
		}
		String getShort() {
			return shortName;
		} 
	}

	public static Map<String, Matcher> getFilterMatcherMap(Map<String,String> valuedFilters) throws Exception{
		Map<String, Matcher> fmm = null;

		if(valuedFilters != null && valuedFilters.size() > 0){

			fmm = new TreeMap<String, Matcher>();

			for(Entry<String,String> e : valuedFilters.entrySet())
			{
				if(	(e == null) ||	(e.getKey() == null) || (e.getValue() == null))
					throw new ShouldntHappenException("Null values in filter, must have not been built properly.");

				fmm.put(e.getKey(), Pattern.compile(e.getValue()).matcher(""));
			}
		}
		return fmm;
	}

	public static void filterOptional(Map<String,Matcher> valuedFilters, List<Map<String, Object>> toBeFiltered) throws Exception{

		for(Entry<String,Matcher> e : valuedFilters.entrySet())
		{
			Matcher curMatcher = e.getValue();
			for(Map<String, Object> m : toBeFiltered){
				String attribute = (String)m.get(e.getKey());
				filterMeasurementListAgainstStringPatternFilter(toBeFiltered, attribute, curMatcher);
			}
		}

	}

	static boolean isMeasurementMatchAgainstMultipleStringPatterns(Map<String,Matcher> valuedFilters, Map<String, Object> m) throws Exception{

		for(Entry<String,Matcher> e : valuedFilters.entrySet())
		{
			Matcher curMatcher = e.getValue();
			if(!isMatchMeasurementAgainstStringPatternFilter(m, curMatcher, e.getKey()))
				return false;
		}
		return true;

	}

	static void filterMeasurementListAgainstStringPatternFilter(List<Map<String, Object>> toBeFiltered, String attribute, Matcher curMatcher) throws Exception {
		for(Map<String, Object> m : toBeFiltered){
			if(!isMatchMeasurementAgainstStringPatternFilter(m, curMatcher, attribute))
				toBeFiltered.remove(m);
		}
	}

	static boolean isMatchMeasurementAgainstStringPatternFilter(Map<String, Object> m, Matcher curMatcher, String atb) throws Exception {

		if((atb == null) || (m == null) || (curMatcher == null))
			return false;
		else{
			String value = (String)m.get(atb);
			if(value != null){
				return isMatchMeasurementAgainstStringPatternFilter(curMatcher, value);
			}
		}
		return false;
	}

	static boolean isMatchMeasurementAgainstStringPatternFilter(Matcher curMatcher, String value) {
		curMatcher.reset(value);
		return curMatcher.matches();
	}

	static long aggregateAverageByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = (long) 0;
		for (Long it : toAgg)
			returned += it;
		return returned / toAgg.size();
	}
	static long aggregateMinByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;

		long returned = toAgg.get(0);
		for (Long it : toAgg)
			if(it < returned)
				returned = it;
		return returned;
	}
	static long aggregateMaxByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = toAgg.get(0);
		for (Long it : toAgg)
			if(it > returned)
				returned = it;
		return returned;
	}
	static long aggregateSumByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = 0;
		for (Long it : toAgg)
			returned += it;
		return returned;
	}
	static long aggregateCountByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		return toAgg.size();
	}

	static long aggregatePercentileByNumericVal(List<Long> toAgg, Double optional) {

		Percentile p = new Percentile();
		if (toAgg == null || toAgg.size() < 1)
			return (long)0;

		return new Double(p.evaluate(getDoubleArrayFromLongList(toAgg), optional)).longValue();
	}

	static long aggregateStandardDevByNumericVal(List<Long> toAgg) {

		StandardDeviation std = new StandardDeviation(false);
		if (toAgg == null || toAgg.size() < 1)
			return (long)0;

		return new Double(std.evaluate(getDoubleArrayFromLongList(toAgg))).longValue();
	}

	static long aggregateStandardDevNoBiasByNumericVal(List<Long> toAgg) {

		StandardDeviation std = new StandardDeviation(true);
		if (toAgg == null || toAgg.size() < 1)
			return (long)0;

		return new Double(std.evaluate(getDoubleArrayFromLongList(toAgg))).longValue();
	}

	static double[] getDoubleArrayFromLongList(List<Long> value) {

		int sizeOfArray = value.size();
		double[] doubleArray = new double[sizeOfArray];

		int i=0;
		for (Long l : value)
		{
			doubleArray[i]= new Double(l);
			i++;
		}

		return doubleArray;
	}


	public static void addMillisecondsToCalWithLong(Calendar c, long milliseconds){

		int maxInt = Long.valueOf(2147483647L).intValue();
		while(milliseconds > 2147483647L){
			c.add(Calendar.MILLISECOND, maxInt);
			milliseconds-=2147483647L;
		}
		c.add(Calendar.MILLISECOND,Long.valueOf(milliseconds).intValue());
	}

	public static Map<String,Long> reduceAll(List<Long> durationList) throws Exception {

		Map<String,Long> result = new TreeMap<String,Long>();
		
		result.put(AggregationType.AVG.getShort(), MeasurementAggregator.aggregateAverageByNumericVal(durationList));
		result.put(AggregationType.COUNT.getShort(), MeasurementAggregator.aggregateCountByNumericVal(durationList));
		result.put(AggregationType.SUM.getShort(), MeasurementAggregator.aggregateSumByNumericVal(durationList));
		result.put(AggregationType.MIN.getShort(), MeasurementAggregator.aggregateMinByNumericVal(durationList));
		result.put(AggregationType.MAX.getShort(), MeasurementAggregator.aggregateMaxByNumericVal(durationList));
		result.put(AggregationType.STD.getShort(), MeasurementAggregator.aggregateStandardDevByNumericVal(durationList));
		result.put(AggregationType.PCL50.getShort(), MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 50D));
		result.put(AggregationType.PCL80.getShort(), MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 80D));
		result.put(AggregationType.PCL90.getShort(), MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 90D));
		result.put(AggregationType.PCL99.getShort(), MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 99D));

		return result;
	}

	public static float computeTpX(long count, long begin, long end, float interval) {
		float fCount = (float) count;
		float fWindow = (float) (end - begin);
		
		if(count > 0)
			//return (long) Math.abs((fCount / fWindow)* interval);
			return Math.abs((fCount / fWindow)* interval);
		return 0L;
	}


}
