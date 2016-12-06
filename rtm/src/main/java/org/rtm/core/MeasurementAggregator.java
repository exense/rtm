package org.rtm.core;

import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.math.stat.descriptive.rank.Percentile;
import org.rtm.commons.Measurement;
import org.rtm.exception.ShouldntHappenException;

public class MeasurementAggregator {

	public static enum AggregationType {
		COUNT,
		MIN,
		MAX,
		SUM,
		AVG,
		PCL
	}

	public static AggregationType getAggTypeFromMetricName(String metricType) throws Exception {

		if(metricType.toLowerCase().equals("avg") || metricType.toLowerCase().equals("average"))
			return AggregationType.AVG;
		if(metricType.toLowerCase().equals("min") || metricType.toLowerCase().equals("minimum"))
			return AggregationType.MIN;
		if(metricType.toLowerCase().equals("max") || metricType.toLowerCase().equals("maximum"))
			return AggregationType.MAX;
		if(metricType.toLowerCase().equals("cnt") || metricType.toLowerCase().equals("count"))
			return AggregationType.COUNT;
		if(metricType.toLowerCase().equals("sum"))
			return AggregationType.SUM;
		if(metricType.toLowerCase().contains("pcl"))
			return AggregationType.PCL;

		throw new Exception("Unknown AggregationType");
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

	public static void filterOptional(Map<String,Matcher> valuedFilters, List<Measurement> toBeFiltered) throws Exception{

		for(Entry<String,Matcher> e : valuedFilters.entrySet())
		{
			Matcher curMatcher = e.getValue();
			for(Measurement t : toBeFiltered){
				String attribute = t.getTextAttribute(e.getKey());
				filterMeasurementListAgainstStringPatternFilter(toBeFiltered, attribute, curMatcher);
			}
		}

	}

	public static boolean isMeasurementMatchAgainstMultipleStringPatterns(Map<String,Matcher> valuedFilters, Measurement t) throws Exception{

		for(Entry<String,Matcher> e : valuedFilters.entrySet())
		{
			Matcher curMatcher = e.getValue();
			if(!isMatchMeasurementAgainstStringPatternFilter(t, curMatcher, e.getKey()))
				return false;
		}
		return true;

	}

	private static void filterMeasurementListAgainstStringPatternFilter(List<Measurement> toBeFiltered, String attribute, Matcher curMatcher) throws Exception {
		for(Measurement t : toBeFiltered){
			if(!isMatchMeasurementAgainstStringPatternFilter(t, curMatcher, attribute))
				toBeFiltered.remove(t);
		}
	}

	private static boolean isMatchMeasurementAgainstStringPatternFilter(Measurement t, Matcher curMatcher, String atb) throws Exception {

		if((atb == null) || (t == null) || (curMatcher == null))
			return false;
		else{
			String value = t.getTextAttribute(atb);
			if(value != null){
				return isMatchMeasurementAgainstStringPatternFilter(curMatcher, value);
			}
		}
		return false;
	}
	
	private static boolean isMatchMeasurementAgainstStringPatternFilter(Matcher curMatcher, String value) {
		curMatcher.reset(value);
		return curMatcher.matches();
	}

	private static long aggregateAverageByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = (long) 0;
		for (Long it : toAgg)
			returned += it;
		return returned / toAgg.size();
	}
	private static long aggregateMinByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;

		long returned = toAgg.get(0);
		for (Long it : toAgg)
			if(it < returned)
				returned = it;
		return returned;
	}
	private static long aggregateMaxByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = toAgg.get(0);
		for (Long it : toAgg)
			if(it > returned)
				returned = it;
		return returned;
	}
	private static long aggregateSumByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		long returned = 0;
		for (Long it : toAgg)
			returned += it;
		return returned;
	}
	private static long aggregateCountByNumericVal(List<Long> toAgg){
		if (toAgg == null || toAgg.size() < 1)
			return 0;
		return toAgg.size();
	}

	private static Long aggregatePercentileByNumericVal(List<Long> toAgg, Double optional) {

		Percentile p = new Percentile();
		if (toAgg == null || toAgg.size() < 1)
			return (long)0;

		return new Double(p.evaluate(getDoubleArrayFromLongList(toAgg), optional)).longValue();
	}

	private static double[] getDoubleArrayFromLongList(List<Long> value) {

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
		
		result.put("avg", MeasurementAggregator.aggregateAverageByNumericVal(durationList));
		result.put("cnt", MeasurementAggregator.aggregateCountByNumericVal(durationList));
		result.put("sum", MeasurementAggregator.aggregateSumByNumericVal(durationList));
		result.put("min", MeasurementAggregator.aggregateMinByNumericVal(durationList));
		result.put("max", MeasurementAggregator.aggregateMaxByNumericVal(durationList));
		result.put("pcl50", MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 50D));
		result.put("pcl80", MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 80D));
		result.put("pcl90", MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 90D));
		result.put("pcl99", MeasurementAggregator.aggregatePercentileByNumericVal(durationList, 99D));

		return result;
	}

}
