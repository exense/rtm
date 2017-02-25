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
package org.rtm.query;

import java.util.List;
import java.util.Properties;
import java.util.UUID;

import org.rtm.request.selection.Selector;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.ResultHandler;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamResultHandler;
import org.rtm.time.RangeBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubQueryCallable extends QueryCallable {
	private static final Logger logger = LoggerFactory.getLogger(SubQueryCallable.class);
	private long subRangeSize;
	
	private Stream<Long> subResults;
	private ParallelRangeExecutor pre;
	private UUID taskId;
	
	public SubQueryCallable(List<Selector> sel, RangeBucket<Long> bucket,
			Properties requestProp, long subRangeSize) {
		super(sel, bucket, requestProp);
		this.taskId = UUID.randomUUID();
		this.subRangeSize = subRangeSize;
		pre = new ParallelRangeExecutor(RangeBucket.toLongTimeInterval(super.bucket), this.subRangeSize);
		//logger.debug("Creating Callable for bucket="+ super.bucket+ "; with subbucket=" + this.subRangeSize);
		this.subResults = new Stream<>();
	}
	
	@Override
	public LongRangeValue call() throws Exception {
		produceAllValuesForRange();
		//TODO:Figure out why parallel merge on concurrent hash map fails (forgets results)
		LongRangeValue lrv = mergeSubResults();
		//logger.debug("[" + this.taskId.toString() + "] Merged data for " + lrv.getStreamPayloadIdentifier().getIdAsTypedObject().toString().substring(7, 13) + " = " +lrv);
		return lrv;
	}
	
	@SuppressWarnings("unchecked")
	private LongRangeValue mergeSubResults() {
		LongRangeValue result = new LongRangeValue(super.bucket);
		subResults.values().stream().forEach(tv -> {
			tv.getData().values().stream().forEach(d -> {
				Dimension dim = (Dimension)d;
				String dimensionValue = dim.getDimensionValue();
				Dimension resDim = result.get(dimensionValue);
				if(resDim == null){
					resDim = new Dimension(dimensionValue);
					result.put(dimensionValue, resDim);
				}
				mergeMetricsForDimension(resDim, dim);
			});
		});
		return result;
	}

	private void mergeMetricsForDimension(Dimension resDim, Dimension dim) {
		// TODO: implement and use single point of accumulation (MeasurementHelper?)
		// merging by "generally" adding for right now
		dim.entrySet().stream().forEach(m -> {
			String metricName = (String) m.getKey();
			Long value = resDim.get(metricName);
			if(value == null){
				value = new Long(m.getValue().longValue());
				resDim.put(metricName, value);
			}
			else{
				long save = value.longValue();
				resDim.remove(metricName);
				resDim.put(metricName, save + m.getValue());
			}
		});
		
	}

	public void produceAllValuesForRange() throws Exception{
		
		ResultHandler<Long> subHandler = new StreamResultHandler(subResults);
		//logger.debug("[" + this.taskId.toString() + "] Producing values now..");
		//TODO: move to unblocking version, get nb threads & timeout from prop
		pre.processRangeSingleLevelBlocking(subHandler, 
				super.sel, prop,
				10, 5L);
	}
	

}
