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

/**
 * @author doriancransac
 * @param <T>
 *
 */
public class StreamComparator<T> {

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

	@SuppressWarnings("unchecked")
	public void compare() {
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

	private AggregationResult<T> diff(AggregationResult<T> value, AggregationResult<T> value2, long l) {
		LongRangeValue result = new LongRangeValue(new RangeBucket<Long>(l, l + this.intervalSize));
		
		for(Object o : value.getDimensionsMap().entrySet())
		{
			Dimension dim1 = ((Entry<String, Dimension>) o).getValue();
			String dimName = ((Entry<String, Dimension>) o).getKey();
			result.put(dimName, metricDiff(dim1, (Dimension)value2.getDimensionsMap().get(dimName)));
		}
		return (AggregationResult<T>)result;
	}

	private Dimension metricDiff(Dimension dim1, Dimension dim2) {
		Dimension res = new Dimension(dim1.getDimensionValue(), dim1.getHistNbPairs(), dim1.getHistApproxMs());
		
		for(Object o : dim1.entrySet())
		{
			Entry<String, Long> e = (Entry<String, Long>) o;
			res.put(e.getKey(), (Long)dim2.get(e.getKey()) - e.getValue());
		}
		return res;
	}

	private void addDiff(AggregationResult<T> diff, long l) {
		this.outStream.getStreamData().put( l, diff);
	}
	
	

}
