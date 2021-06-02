package org.rtm.db;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;
import java.util.stream.Stream;

import step.core.collections.Filter;
import step.core.collections.Filters;
import step.core.collections.SearchOrder;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.measurement.MeasurementHelper;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.selection.Selector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class QueryClient {

	private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);
	private Properties prop;
	private MeasurementAccessor ma;

	public QueryClient(Properties prop, MeasurementAccessor ma) {
		this.ma = ma;
		this.prop = prop;
	}

	@SuppressWarnings("rawtypes")
	public Stream<? extends Map> executeQuery(Filter timelessQuery) {
		return ma.find(timelessQuery);
	}

	@SuppressWarnings("rawtypes")
	public Stream<? extends Map> executeAdvancedQuery(Filter timelessQuery) {
		ArrayList<String> fields = new ArrayList<>();
		fields.add((String) prop.get("aggregateService.timeField"));
		fields.add((String) prop.get("aggregateService.valueField"));
		MeasurementHelper mh = new MeasurementHelper(prop);
		fields.addAll(mh.getSplitDimensions());
		return ma.advancedFind(timelessQuery, fields);
	}

	@SuppressWarnings("rawtypes")
	public Stream<? extends Map> executeQuery(Filter timelessQuery, String sortKey, Integer sortDirection) {
		return ma.find(timelessQuery, new SearchOrder(sortKey, sortDirection));
	}

	@SuppressWarnings("rawtypes")
	public Stream<? extends Map> executeQuery(Filter timelessQuery, String sortKey, Integer sortDirection, int skip,
			int limit) {
		return ma.find(timelessQuery, new SearchOrder(sortKey, sortDirection), skip, limit);
	}

	public FilterQuery buildQuery(List<Selector> sel, LongTimeInterval bucket) {
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		FilterQuery aQuery = new FilterQuery(sel, timeField, timeFormat);
		FilterQuery query = new FilterQuery();
		query.setQuery(mergeTimelessWithTimeCriterion(aQuery.getQuery(), buildTimeCriterion(bucket)));
		return query;
	}

	public Filter mergeTimelessWithTimeCriterion(Filter timelessQuery, Filter timeCriterion) {
		return Filters.and(List.of(timelessQuery,timeCriterion));
	}

	public Filter buildTimeCriterion(LongTimeInterval bucket) {
		List<Filter> criteria = new ArrayList<>();
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		Long min;
		Long max;
		if (timeFormat.equals("date")) {
			throw new RuntimeException("Date format is not supported");
			//min = new Date(bucket.getBegin());
			//max = new Date(bucket.getEnd());
		} else {
			min = bucket.getBegin();
			max = bucket.getEnd();
		}
		return Filters.and(List.of(Filters.gte(timeField,min),Filters.lt(timeField,max)));
	}

	@SuppressWarnings("rawtypes")
	public LongTimeInterval findEffectiveBoundariesViaMongo(LongTimeInterval lti, List<Selector> sel) throws Exception {
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");

		FilterQuery query = new FilterQuery(sel, timeField, timeFormat);
		
		Map min = null;
		Map max = null;
		try {
			min = executeQuery(query.getQuery(), timeField, 1).findFirst().get();
			max = executeQuery(query.getQuery(), timeField, -1).findFirst().get();
		} catch (NoSuchElementException e) {
			return new LongTimeInterval(0L, 1L);
		}
		Object maxobject = max.get(timeField);
		Object minobject = min.get(timeField);

		Long maxVal = maxobject instanceof Long ? (Long) maxobject : ((Date) maxobject).getTime();
		Long minVal = minobject instanceof Long ? (Long) minobject : ((Date) minobject).getTime();

		long result = maxVal - minVal;
		// logger.debug("time window : " + maxVal + " - " + minVal + " = " + result);

		if (result < 0L) {
			throw new Exception("Could not compute auto-granularity (negative value): result=" + result);
		}
		return new LongTimeInterval(minVal, maxVal + 1);
	}

	public static long computeOptimalIntervalSize(long timeWindow, int targetSeriesDots) {
		return Math.abs(timeWindow / targetSeriesDots) + 1;
	}

	public long run90PclOnFirstSample(int heuristicSampleSize, List<Selector> sel) {
		logger.debug("Starting sampling of first " + heuristicSampleSize + " data points...");
		long start = System.currentTimeMillis();
		String timeField = (String) prop.get("aggregateService.timeField");
		String timeFormat = (String) prop.get("aggregateService.timeFormat");
		FilterQuery query = new FilterQuery(sel, timeField, timeFormat);

		try {
			//duplicated values must be retained for percentile calculations, replacing set by list
			List<Long> sortedValues = new ArrayList<Long>();

			Stream<? extends Map> stream = executeQuery(query.getQuery(), timeField, 1, 0, heuristicSampleSize);
			
			stream.forEach(o -> {
				Map dot = (Map) o;
				if (dot != null) {
					String valueField = (String) prop.get("aggregateService.valueField");
					Object object = dot.get(valueField);
					if (object == null) {
						throw new RuntimeException("No value for valueField:" + valueField);
					}
					sortedValues.add((Long) object);
				}
			});

			sortedValues.sort(null);
			int position = Math.round(sortedValues.size() * 0.9F);

			logger.debug("Sampling complete. Duration was " + (System.currentTimeMillis() - start) + " ms.");

			if (sortedValues.size() > 0) {
				if (position >= sortedValues.size()) {
					return sortedValues.get((sortedValues.size() - 1));
				}
				else {
					return sortedValues.get(position);
				}
			// temporarily handling no data case this way
			} else {
				return 1L;
			}

		} catch (NoSuchElementException e) {
			return 1L;
		}
	}
}
