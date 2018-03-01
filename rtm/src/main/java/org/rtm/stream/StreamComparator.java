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
package org.rtm.stream;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentSkipListMap;

import org.rtm.range.RangeBucket;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 * @param <T>
 *
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class StreamComparator<T> {

	private static final Logger logger = LoggerFactory.getLogger(StreamComparator.class);

	private Stream s1;
	private Stream s2;
	private Stream outStream;
	private long intervalSize;

	public StreamComparator(Stream s1, Stream s2, Stream outStream, long intervalSize) {
		this.s1 = s1;
		this.s2 = s2;
		this.outStream = outStream;
		this.intervalSize = intervalSize;
	}

	public void compare() throws Exception {
		ConcurrentSkipListMap<Long, AggregationResult<T>> data1 = s1.getStreamData();
		ConcurrentSkipListMap<Long, AggregationResult<T>> data2 = s2.getStreamData();

		Iterator<Entry<Long, AggregationResult<T>>> it1 = data1.entrySet().iterator();
		Iterator<Entry<Long, AggregationResult<T>>> it2 = data2.entrySet().iterator();

		long currRange = 0L;

		while(it1.hasNext() && it2.hasNext()){
			Entry<Long, AggregationResult<T>> res1 = it1.next();
			Entry<Long, AggregationResult<T>> res2 = it2.next();

			addDiff(diff(res1.getValue(), res2.getValue(), currRange), currRange);

			currRange+=this.intervalSize;
		}

	}

	private AggregationResult<T> diff(AggregationResult<T> value, AggregationResult<T> value2, long l) throws Exception {
		LongRangeValue result = new LongRangeValue(new RangeBucket<Long>(l, l + this.intervalSize).getLowerBound());

		for(Object o : value.getDimensionsMap().entrySet())
		{
			WorkDimension dim1 = ((Entry<String, WorkDimension>) o).getValue();
			String dimName = ((Entry<String, WorkDimension>) o).getKey();
			if(value2.getDimensionsMap() == null)
				throw new Exception("Null dimension map for value2="+ value2);
			WorkDimension compareTo = (WorkDimension)value2.getDimensionsMap().get(dimName);
			WorkDimension dimResult = null;
			try{
				dimResult = dim1.diff(compareTo);
			}catch(NoDimensionException e){
				logger.debug("Dimension=" +dimName+ " not found, skipping.", e);
				continue;
			}
			result.put(dimName, dimResult);
		}
		return (AggregationResult<T>)result;
	}

	private void addDiff(AggregationResult<T> diff, long l) {
		this.outStream.getStreamData().put( l, diff);
	}



}
