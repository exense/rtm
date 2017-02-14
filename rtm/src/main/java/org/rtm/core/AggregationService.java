/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.core;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.UUID;

import org.rtm.commons.Configuration;
import org.rtm.core.ComplexServiceResponse.Status;
import org.rtm.exception.NoDataException;
import org.rtm.exception.ShouldntHappenException;
import org.rtm.rest.aggregation.AggOutput;
import org.rtm.rest.aggregation.AggregationHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unchecked"})
public class AggregationService{

	private static final Logger logger = LoggerFactory.getLogger(AggregationService.class);
	
	private static final int maxCapacityForInterval = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxCapacityForInterval"));
	private static final int maxAggregatesForSeries = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxAggregatesForSeries"));
	private static final int maxSeries = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxSeries"));
	public static final int maxMeasurements = Configuration.getInstance().getPropertyAsInteger("aggregateService.maxMeasurements");
	private boolean isDebug = false;

	public AggregationService(){
		if(Configuration.getInstance().getProperty("rtm.debug") != null)
			this.isDebug  = Configuration.getInstance().getProperty("rtm.debug").equals("true");
	}

	public ComplexServiceResponse buildAggregatesForTimeInconsistent(
			String sessionId, Iterable<Map<String, Object>> ble, long granularity,
			String differenciatorKey, String beginKey, String endKey, String valueKey, String sessionKey
			) throws Exception{
		// ASSUMING THE INCOMING DATA IS SORTED BY DATE

		ComplexServiceResponse resp = new ComplexServiceResponse();
		int totalProcessedTransactions = 0;
		boolean reachedSeriesWarningThreshold = false;
		boolean capacityWarningReached = false;

		Map<String,List<Map<String, Object>>> res = new TreeMap<String,List<Map<String, Object>>>();
		Map<String,List<Map<String, Object>>> subChunk = new TreeMap<String,List<Map<String, Object>>>();

		Iterator<Map<String, Object>> it = ble.iterator();
		Map<String, Object> m = null;
		Long firstBegin = null;
		if(it.hasNext())
			m = it.next();
		if(m == null)
			throw new NoDataException("No data to work with.");

		firstBegin = (Long)m.get(beginKey);
		totalProcessedTransactions++;
		Long start;

		boolean noDiffMode = false;
		if(differenciatorKey == null || differenciatorKey.isEmpty()){
			differenciatorKey = UUID.randomUUID().toString();
			noDiffMode = true;
		}

		start = (Long)m.get(beginKey);

		LongTimeInterval curr = new LongTimeInterval(start, granularity);
		while(!curr.belongs(firstBegin))
			curr = curr.getNext(granularity);

		int totalChunkSize = 0;
		// Iterate over all transactions in the result set

		int nbProcessedIntervals = 0;
		while(true)
		{
			if(isDebug)
				System.out.println("for=>["+m+"]");

			// The currently evaluated measurement belongs to the current interval
			if(curr.belongs((Long) m.get(beginKey))){
				/* 211014 */
				if(noDiffMode || m.get(differenciatorKey) == null)
					m.put(differenciatorKey, "[DefaultGroupBy]");
				/* /211014 */
				if(subChunk.get(m.get(differenciatorKey)) == null){

					if((maxSeries > 0) && ((subChunk.keySet().size() >= maxSeries) || (res.keySet().size() >= maxSeries))){
						reachedSeriesWarningThreshold = true;
					}else{
						subChunk.put((String)m.get(differenciatorKey), new ArrayList<Map<String, Object>>());

						// Limit space taken by a single call (i.e request)
						if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
						{						
							subChunk.get(m.get(differenciatorKey)).add(m);
							totalChunkSize++;
						}else{
							if(maxCapacityForInterval > 0)
								capacityWarningReached = true;
						}
					}
				}else{
					// Limit space taken by a single call (i.e request)
					if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
					{						
						subChunk.get(m.get(differenciatorKey)).add(m);
						totalChunkSize++;
					}else{
						if(maxCapacityForInterval > 0)
							capacityWarningReached = true;
					}
				}
				// The currently evaluated Map<String, Object> does not belong to current interval
			}else{
				// Should not be possible 
				if((Long) m.get(beginKey) < curr.getBegin())
					throw new ShouldntHappenException("We somehow ended up with a transaction that belongs to an old, already processed interval");

				/* 211014 */
				if(noDiffMode || m.get(differenciatorKey) == null)
					m.put(differenciatorKey, "[DefaultGroupBy]");

				/* /211014 */

				// If needed, initialize result list for the currently evaluated transaction
				if(res.get(m.get(differenciatorKey)) == null)
					res.put((String)m.get(differenciatorKey), new ArrayList<Map<String, Object>>());

				// Process results and flush buffer for all known transactions in buffer
				for(Entry<String,List<Map<String, Object>>> e : subChunk.entrySet())
				{
					List<Map<String, Object>> dataList = e.getValue();
					String tn = e.getKey();
					if(dataList.size() > 0){
						if(res.get(tn) == null)
							res.put(tn, new ArrayList<Map<String, Object>>());
						Map<String, Object> agg = new HashMap<String, Object>();
						agg.put(differenciatorKey, tn);
						agg.put(sessionKey, sessionId);
						agg.put(beginKey, curr.getBegin());
						agg.put(endKey, curr.getEnd());
						agg.putAll(MeasurementAggregator.reduceAll(AggregationHelper.getDurationsList(dataList, valueKey)));
						res.get(tn).add(agg);

						subChunk.get(tn).clear();
						totalChunkSize = 0;
					}
				}

				// Shift interval to relevant, current transaction's
				while(!curr.belongs((Long) m.get(beginKey))){
					curr = curr.getNext(granularity);
					nbProcessedIntervals++;
					if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries))
						break;
				}

				if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries))
					break;

				// Add current transaction to proper interval
				if(subChunk.get(m.get(differenciatorKey)) == null){

					if((maxSeries > 0) && ((subChunk.keySet().size() >= maxSeries) || (res.keySet().size() >= maxSeries))){
						reachedSeriesWarningThreshold = true;
					}else{
						subChunk.put((String)m.get(differenciatorKey), new ArrayList<Map<String, Object>>());
						// Limit space taken by a single call (i.e request)
						if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
						{						
							subChunk.get(m.get(differenciatorKey)).add(m);
							totalChunkSize++;
						}else{
							if(maxCapacityForInterval > 0)
								capacityWarningReached = true;
						}
					}
				}else{
					// Limit space taken by a single call (i.e request)
					if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
					{						
						subChunk.get(m.get(differenciatorKey)).add(m);
						totalChunkSize++;
					}else{
						if(maxCapacityForInterval > 0)
							capacityWarningReached = true;
					}
				}

			}

			try{
				m = it.next();
				totalProcessedTransactions++;
			}catch(NoSuchElementException e){break;}
		}

		// Take care of crumbs for the various ways of exiting the loop when some data remains in the buffer
		for(Entry<String,List<Map<String, Object>>> e : subChunk.entrySet())
		{
			List<Map<String, Object>> dataList = e.getValue();
			String tn = e.getKey();
			if(dataList.size() > 0){
				if(res.get(tn) == null)
					res.put(tn, new ArrayList<Map<String, Object>>());
				Map<String, Object> agg = new HashMap<String, Object>();
				agg.put(differenciatorKey, tn);
				agg.put(sessionKey, sessionId);
				agg.put(beginKey, curr.getBegin());
				agg.put(endKey, curr.getEnd());
				agg.putAll(MeasurementAggregator.reduceAll(AggregationHelper.getDurationsList(dataList, valueKey)));
				res.get(tn).add(agg);
				subChunk.get(tn).clear();
			}
		}

		resp.setPayload(res);
		resp.setReturnStatus(Status.NORMAL);

		if(maxSeries > 0 && reachedSeriesWarningThreshold)
		{
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of series (" +maxSeries+ ") has been reached. Certain groupby keys were not processed.; ");
		}

		if(maxMeasurements > 0 && totalProcessedTransactions == maxMeasurements){
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of Map<String, Object>s to be processed (" +maxMeasurements+ ") has been reached. Certain Map<String, Object>s were not processed.; ");
		}

		if((maxCapacityForInterval > 0) && capacityWarningReached){
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of Map<String, Object>s per interval (" + maxCapacityForInterval + ") has been reached. Certain Map<String, Object>s were ignored in the aggregated results. Try decreasing the value of field \"granularity\" or select a smaller interval.; ");
		}
		if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries)){
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of Aggregates (" + maxAggregatesForSeries + ") has been reached. Certain Aggregates were ignored in the aggregated results. Try increasing the value of field \"granularity\" or select a smaller interval.; ");
		}
		return resp;
	}

	public static ComplexServiceResponse makeDataConsistent(ComplexServiceResponse input, 
			String sessionKey, String beginKey, String endKey, String nameKey	) throws Exception{

		Map<String,List<Map<String, Object>>> inconsistent = input.getPayload();

		if(inconsistent == null || inconsistent.size() <1)
			throw new NoDataException("No data to work with.");

		List<Map<String, Object>> firstList = (List<Map<String, Object>>) ((Entry<String, List<Map<String, Object>>>) inconsistent.entrySet().toArray()[0]).getValue();

		if(firstList == null || firstList.size() < 1 || firstList.get(0) == null)
			throw new ShouldntHappenException("Can't access a proper first element :" + firstList);

		Map<String, Object> first = firstList.get(0);

		long granularity = (Long)first.get(endKey) - (Long)first.get(beginKey);

		Long[] minmax = findMinMax(inconsistent, beginKey);
		Long min = minmax[0]; 
		Long max = minmax[1];

		Map<String,List<Map<String, Object>>> consistent = new TreeMap<String,List<Map<String, Object>>>();

		for(Entry<String,List<Map<String, Object>>> e : inconsistent.entrySet())
		{
			String t = e.getKey();
			List<Map<String, Object>> lt = e.getValue();
			List<Map<String, Object>> filledGaps = fillInTheGaps(lt, min, max, granularity, beginKey, endKey, sessionKey, nameKey);
			// check for maxAggregates?
			consistent.put(t, filledGaps);
		}

		input.setPayload(consistent);

		return input;
	}


	private static List<Map<String, Object>> fillInTheGaps(List<Map<String, Object>> lr, Long min, Long max, long granularity,
			String beginKey, String endKey, String sessionKey, String nameKey) throws Exception {

		if(lr == null || lr.size() < 1)
			throw new NoDataException("No data to work with.");

		if ( Math.round((max - min) / granularity) > 100000)
			throw new Exception("The input dates and granularity result in too many datapoints (filled blanks)");

		Map<String, Object> first = lr.get(0);

		List<Map<String, Object>> lf = new ArrayList<Map<String, Object>>();

		long initialGap = min;
		long curTime = (Long) first.get(beginKey);
		while(curTime > initialGap){

			BlankAggregate b = new BlankAggregate();
			b.put(sessionKey, first.get(sessionKey));
			b.put(nameKey,first.get(nameKey));
			b.put(beginKey, initialGap);
			b.put(endKey, initialGap + granularity);
			lf.add(b);
			initialGap+=granularity;
		}

		Map<String, Object> last = first;
		for(Map<String, Object> a : lr){

			long cursor = (Long) last.get(beginKey);
			while(((Long)a.get(beginKey)) - cursor > granularity){
				cursor+=granularity;				
				BlankAggregate b = new BlankAggregate();
				b.put(sessionKey, first.get(sessionKey));
				b.put(nameKey,first.get(nameKey));
				b.put(beginKey, cursor);
				b.put(endKey, cursor + granularity);
				lf.add(b);
			}

			lf.add(a);
			last = a;
		}

		Date lastDate = new Date((Long)last.get(beginKey) + granularity);

		long finalGap = max;
		curTime = lastDate.getTime();
		while(curTime <= finalGap){
			BlankAggregate b = new BlankAggregate();
			b.put(sessionKey, first.get(sessionKey));
			b.put(nameKey,(String)first.get(nameKey));
			b.put(beginKey, curTime);
			b.put(endKey, curTime + granularity);
			lf.add(b);
			curTime+=granularity;
		}

		return lf;
	}

	private static Long[] findMinMax(Map<String, List<Map<String, Object>>> inconsistent,
			String beginKey) throws Exception {
		Long min;
		Long max;

		if(inconsistent == null || inconsistent.size() < 1)
			throw new NoDataException("No data to work with");

		Entry<String, List<Map<String, Object>>> first = (Entry<String, List<Map<String, Object>>>) inconsistent.entrySet().toArray()[0];
		List<Map<String, Object>> fl = (List<Map<String, Object>>) first.getValue();

		if(fl == null || fl.size() < 1)
			throw new NoDataException("No data in Series");

		min = (Long) fl.get(0).get(beginKey);
		max = min;

		for(Entry e : inconsistent.entrySet())
		{
			List<Map<String, Object>> lt = (List<Map<String, Object>>)e.getValue();

			if(lt == null || lt.size() < 1)
				throw new NoDataException("No data in Series");

			Long thisMin = (Long) lt.get(0).get(beginKey); 
			Long thisMax = (Long) lt.get(lt.size()-1).get(beginKey);

			if(min > thisMin)
				min = thisMin;

			if(max < thisMax)
				max = thisMax;
		}

		Long[] minmax = {min,max};

		return minmax;
	}

	public static AggOutput convertForJson(Map<String, List<Map<String, Object>>> data){

		AggOutput so = new AggOutput();
		List<AggregateResult> res = new ArrayList<AggregateResult>();

		for(Entry<String, List<Map<String, Object>>> e : data.entrySet())
		{
			AggregateResult ar = new AggregateResult();
			ar.setGroupby(e.getKey());
			ar.setData(e.getValue());
			res.add(ar);
		}
		so.setPayload(res);
		return so;
	}

	public long computeAutoGranularity(long timeWindow, int targetSeriesDots) {
		long result = Math.abs(timeWindow / targetSeriesDots);
		logger.debug("auto-granularity : abs(" + timeWindow + " / " + targetSeriesDots + " ) = " + result);
		return result;
	}
}
