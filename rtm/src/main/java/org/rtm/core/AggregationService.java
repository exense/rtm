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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.TreeMap;
import java.util.UUID;

import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.commons.MeasurementHelper;
import org.rtm.core.ComplexServiceResponse.Status;
import org.rtm.exception.NoDataException;
import org.rtm.exception.ShouldntHappenException;
import org.rtm.rest.ServiceOutput;

public class AggregationService{

	private static final int maxCapacityForInterval = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxCapacityForInterval"));
	private static final int maxAggregatesForSeries = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxAggregatesForSeries"));
	private static final int maxSeries = Integer.parseInt(Configuration.getInstance().getProperty("aggregateService.maxSeries"));
	public static final int maxMeasurements = Configuration.getInstance().getPropertyAsInteger("aggregateService.maxMeasurements");
	private boolean isDebug = false;

	public AggregationService(){
		if(Configuration.getInstance().getProperty("rtm.debug") != null)
			this.isDebug  = Configuration.getInstance().getProperty("rtm.debug").equals("true");
	}

	public ComplexServiceResponse buildAggregatesForTimeInconsistent(String sessionId, Iterable<Measurement> ble,long granularity,
			String differenciatorKey, String beginKey,  String endKey, String valueKey, String sessionKey
			) throws Exception{
		return buildAggregatesForTimeInconsistent(
				sessionId, ble, null, null, granularity,
				differenciatorKey, beginKey, endKey, valueKey, sessionKey
				);
	}

	public ComplexServiceResponse buildAggregatesForTimeInconsistent(
			String sessionId, Iterable<Measurement> ble, Date pStart, Date pEnd, long granularity,
			String differenciatorKey, String beginKey, String endKey, String valueKey, String sessionKey
			) throws Exception{
		// ASSUMING THE INCOMING DATA IS SORTED BY DATE

		ComplexServiceResponse resp = new ComplexServiceResponse();
		int totalProcessedTransactions = 0;
		boolean reachedSeriesWarningThreshold = false;
		boolean capacityWarningReached = false;

		Map<String,List<Measurement>> res = new TreeMap<String,List<Measurement>>();
		Map<String,List<Measurement>> subChunk = new TreeMap<String,List<Measurement>>();

		Iterator<Measurement> it = ble.iterator();
		Measurement t = null;
		Long firstBegin = null;
		if(it.hasNext())
			t = it.next();
		if(t == null)
			throw new NoDataException("No data to work with.");

		firstBegin = t.getNumericalAttribute(beginKey);
		totalProcessedTransactions++;
		Long start;

		boolean noDiffMode = false;
		if(differenciatorKey == null || differenciatorKey.isEmpty()){
			differenciatorKey = UUID.randomUUID().toString();
			noDiffMode = true;
		}
		if(pStart == null)
			start = t.getNumericalAttribute(beginKey);
		else
			start = pStart.getTime();

		LongTimeInterval curr = new LongTimeInterval(start, granularity);
		while(!curr.belongs(firstBegin))
			curr = curr.getNext(granularity);

		int totalChunkSize = 0;
		// Iterate over all transactions in the result set

		int nbProcessedIntervals = 0;
		while(true)
		{
			if(pEnd != null){
				if(t.getNumericalAttribute(beginKey) + t.getNumericalAttribute(valueKey) > pEnd.getTime())
					break;
			}

			if(isDebug)
				System.out.println("for=>["+t+"]");

			// The currently evaluated Measurement belongs to current interval
			if(curr.belongs(t.getNumericalAttribute(beginKey))){
				/* 211014 */
				if(noDiffMode || t.getTextAttribute(differenciatorKey) == null)
					t.setTextAttribute(differenciatorKey, "[DefaultGroupBy]");
				/* /211014 */
				if(subChunk.get(t.getTextAttribute(differenciatorKey)) == null){

					if((maxSeries > 0) && ((subChunk.keySet().size() >= maxSeries) || (res.keySet().size() >= maxSeries))){
						reachedSeriesWarningThreshold = true;
					}else{
						subChunk.put(t.getTextAttribute(differenciatorKey), new ArrayList<Measurement>());

						// Limit space taken by a single call (i.e request)
						if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
						{						
							subChunk.get(t.getTextAttribute(differenciatorKey)).add(t);
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
						subChunk.get(t.getTextAttribute(differenciatorKey)).add(t);
						totalChunkSize++;
					}else{
						if(maxCapacityForInterval > 0)
							capacityWarningReached = true;
					}
				}
				// The currently evaluated Measurement does not belong to current interval
			}else{
				// Should not be possible 
				if(t.getNumericalAttribute(beginKey) < curr.getBegin())
					throw new ShouldntHappenException("We somehow ended up with a transaction that belongs to an old, already processed interval");

				/* 211014 */
				if(noDiffMode || t.getTextAttribute(differenciatorKey) == null)
					t.setTextAttribute(differenciatorKey, "[DefaultGroupBy]");

				/* /211014 */

				// If needed, initialize result list for the currently evaluated transaction
				if(res.get(t.getTextAttribute(differenciatorKey)) == null)
					res.put(t.getTextAttribute(differenciatorKey), new ArrayList<Measurement>());

				// Process results and flush buffer for all known transactions in buffer
				for(Entry<String,List<Measurement>> e : subChunk.entrySet())
				{
					List<Measurement> dataList = e.getValue();
					String tn = e.getKey();
					if(dataList.size() > 0){
						if(res.get(tn) == null)
							res.put(tn, new ArrayList<Measurement>());
						Measurement agg = new Measurement();
						agg.setTextAttribute(differenciatorKey, tn);
						agg.setTextAttribute(sessionKey, sessionId);
						agg.setNumericalAttribute(beginKey, curr.getBegin());
						agg.setNumericalAttribute(endKey, curr.getEnd());
						agg.setNumericalAttributes(MeasurementAggregator.reduceAll(MeasurementHelper.getDurationsList(dataList, valueKey)));
						res.get(tn).add(agg);

						subChunk.get(tn).clear();
						totalChunkSize = 0;
					}
				}

				// Shift interval to relevant, current transaction's
				while(!curr.belongs(t.getNumericalAttribute(beginKey))){
					curr = curr.getNext(granularity);
					nbProcessedIntervals++;
					if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries))
						break;
				}

				if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries))
					break;

				// Add current transaction to proper interval
				if(subChunk.get(t.getTextAttribute(differenciatorKey)) == null){

					if((maxSeries > 0) && ((subChunk.keySet().size() >= maxSeries) || (res.keySet().size() >= maxSeries))){
						reachedSeriesWarningThreshold = true;
					}else{
						subChunk.put(t.getTextAttribute(differenciatorKey), new ArrayList<Measurement>());
						// Limit space taken by a single call (i.e request)
						if((maxCapacityForInterval < 1) || (totalChunkSize < maxCapacityForInterval))
						{						
							subChunk.get(t.getTextAttribute(differenciatorKey)).add(t);
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
						subChunk.get(t.getTextAttribute(differenciatorKey)).add(t);
						totalChunkSize++;
					}else{
						if(maxCapacityForInterval > 0)
							capacityWarningReached = true;
					}
				}

			}

			try{
				t = it.next();
				totalProcessedTransactions++;
			}catch(NoSuchElementException e){break;}
		}

		// Take care of crumbs for the various ways of exiting the loop when some data remains in the buffer
		for(Entry<String,List<Measurement>> e : subChunk.entrySet())
		{
			List<Measurement> dataList = e.getValue();
			String tn = e.getKey();
			if(dataList.size() > 0){
				if(res.get(tn) == null)
					res.put(tn, new ArrayList<Measurement>());
				Measurement agg = new Measurement();
				agg.setTextAttribute(differenciatorKey, tn);
				agg.setTextAttribute(sessionKey, sessionId);
				agg.setNumericalAttribute(beginKey, curr.getBegin());
				agg.setNumericalAttribute(endKey, curr.getEnd());
				agg.setNumericalAttributes(MeasurementAggregator.reduceAll(MeasurementHelper.getDurationsList(dataList, valueKey)));
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
			resp.setMessage(resp.getMessage() + "The maximum number of Measurements to be processed (" +maxMeasurements+ ") has been reached. Certain Measurements were not processed.; ");
		}

		if((maxCapacityForInterval > 0) && capacityWarningReached){
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of Measurements per interval (" + maxCapacityForInterval + ") has been reached. Certain Measurements were ignored in the aggregated results. Try decreasing the value of field \"granularity\" or select a smaller interval.; ");
		}
		if((maxAggregatesForSeries > 0) && (nbProcessedIntervals >= maxAggregatesForSeries)){
			resp.setReturnStatus(Status.WARNING);
			resp.setMessage(resp.getMessage() + "The maximum number of Aggregates (" + maxAggregatesForSeries + ") has been reached. Certain Aggregates were ignored in the aggregated results. Try increasing the value of field \"granularity\" or select a smaller interval.; ");
		}
		return resp;
	}

	@SuppressWarnings("unchecked")
	public static ComplexServiceResponse makeDataConsistent(ComplexServiceResponse input, 
			String sessionKey, String beginKey, String endKey, String nameKey	) throws Exception{

		Map<String,List<Measurement>> inconsistent = input.getPayload();

		if(inconsistent == null || inconsistent.size() <1)
			throw new NoDataException("No data to work with.");

		List<Measurement> firstList = (List<Measurement>) ((Entry<String, List<Measurement>>) inconsistent.entrySet().toArray()[0]).getValue();

		if(firstList == null || firstList.size() < 1 || firstList.get(0) == null)
			throw new ShouldntHappenException("Can't access a proper first element :" + firstList);

		Measurement first = firstList.get(0);

		long granularity = first.getNumericalAttribute(endKey) - first.getNumericalAttribute(beginKey);

		Long[] minmax = findMinMax(inconsistent, beginKey);
		Long min = minmax[0]; 
		Long max = minmax[1];

		Map<String,List<Measurement>> consistent = new TreeMap<String,List<Measurement>>();

		for(Entry<String,List<Measurement>> e : inconsistent.entrySet())
		{
			String t = e.getKey();
			List<Measurement> lt = e.getValue();
			List<Measurement> filledGaps = fillInTheGaps(lt, min, max, granularity, beginKey, endKey, sessionKey, nameKey);
			// check for maxAggregates?
			consistent.put(t, filledGaps);
		}

		input.setPayload(consistent);

		return input;
	}


	private static List<Measurement> fillInTheGaps(List<Measurement> lr, Long min, Long max, long granularity,
			String beginKey, String endKey, String sessionKey, String nameKey) throws Exception {

		if(lr == null || lr.size() < 1)
			throw new NoDataException("No data to work with.");

		if ( Math.round((max - min) / granularity) > 100000)
			throw new Exception("The input dates and granularity result in too many datapoints (filled blanks)");

		Measurement first = lr.get(0);

		List<Measurement> lf = new ArrayList<Measurement>();

		long initialGap = min;
		long curTime = first.getNumericalAttribute(beginKey);
		while(curTime > initialGap){

			BlankAggregate b = new BlankAggregate();
			b.setTextAttribute(sessionKey, first.getTextAttribute(sessionKey));
			b.setTextAttribute(nameKey,first.getTextAttribute(nameKey));
			b.setNumericalAttribute(beginKey, initialGap);
			b.setNumericalAttribute(endKey, initialGap + granularity);
			lf.add(b);
			initialGap+=granularity;
		}

		Measurement last = first;
		for(Measurement a : lr){

			long cursor = last.getNumericalAttribute(beginKey);
			while(a.getNumericalAttribute(beginKey) - cursor > granularity){
				cursor+=granularity;				
				BlankAggregate b = new BlankAggregate();
				b.setTextAttribute(sessionKey, first.getTextAttribute(sessionKey));
				b.setTextAttribute(nameKey,first.getTextAttribute(nameKey));
				b.setNumericalAttribute(beginKey, cursor);
				b.setNumericalAttribute(endKey, cursor + granularity);
				lf.add(b);
			}

			lf.add(a);
			last = a;
		}

		Date lastDate = new Date(last.getNumericalAttribute(beginKey) + granularity);

		long finalGap = max;
		curTime = lastDate.getTime();
		while(curTime <= finalGap){
			BlankAggregate b = new BlankAggregate();
			b.setTextAttribute(sessionKey, first.getTextAttribute(sessionKey));
			b.setTextAttribute(nameKey,first.getTextAttribute(nameKey));
			b.setNumericalAttribute(beginKey, curTime);
			b.setNumericalAttribute(endKey, curTime + granularity);
			lf.add(b);
			curTime+=granularity;
		}

		return lf;
	}


	@SuppressWarnings({ "rawtypes", "unchecked" })
	private static Long[] findMinMax(Map<String, List<Measurement>> inconsistent,
			String beginKey) throws Exception {
		Long min;
		Long max;

		if(inconsistent == null || inconsistent.size() < 1)
			throw new NoDataException("No data to work with");

		Entry<String, List<Measurement>> first = (Entry<String, List<Measurement>>) inconsistent.entrySet().toArray()[0];
		List<Measurement> fl = (List<Measurement>) first.getValue();

		if(fl == null || fl.size() < 1)
			throw new NoDataException("No data in Series");

		min = fl.get(0).getNumericalAttribute(beginKey);
		max = min;

		for(Entry e : inconsistent.entrySet())
		{
			List<Measurement> lt = (List<Measurement>)e.getValue();

			if(lt == null || lt.size() < 1)
				throw new NoDataException("No data in Series");

			Long thisMin =lt.get(0).getNumericalAttribute(beginKey); 
			Long thisMax = lt.get(lt.size()-1).getNumericalAttribute(beginKey);

			if(min > thisMin)
				min = thisMin;

			if(max < thisMax)
				max = thisMax;
		}

		Long[] minmax = {min,max};

		return minmax;
	}

	public static ServiceOutput convertForJson(Map<String, List<Measurement>> data){

		ServiceOutput so = new ServiceOutput();
		List<AggregateResult> res = new ArrayList<AggregateResult>();

		for(Entry<String, List<Measurement>> e : data.entrySet())
		{
			AggregateResult ar = new AggregateResult();
			ar.setGroupby(e.getKey());
			ar.setData(e.getValue());
			res.add(ar);
		}
		so.setPayload(res);
		return so;
	}
}
