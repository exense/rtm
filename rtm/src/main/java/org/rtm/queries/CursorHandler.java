package org.rtm.queries;

import java.util.HashMap;
import java.util.Map;

import org.rtm.buckets.RangeBucket;
import org.rtm.results.AggregationResult;

public class CursorHandler{

	public AggregationResult handle(Iterable<? extends Map> iterable, RangeBucket<Long> myBucket) {
		
		// we'll only do counts for now
		
		int count = 0;
		for(Map<String, Object> m : iterable){
			count++;
			//incremental stats go here
		}
		
		// TODO: produce a "dimension" data point
		
		return null;
	}

}
