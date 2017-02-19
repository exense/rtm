package org.rtm.results;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ResultHandler {

	private ConcurrentMap<String,Map<Long, Map<String,Object>>> resultStream = new ConcurrentHashMap<>();

	public void attachResult(AggregationResult r) {
		Map<Long, Map<String,Object>> dimensionData = resultStream.get(r.getDimension());
		if(dimensionData == null)
			dimensionData = new ConcurrentHashMap<>();
		dimensionData.put(r.getIntervalBegin(), r.getPayload());
	}

	public ConcurrentMap getStreamHandle() {
		return resultStream;
	}
	
}
