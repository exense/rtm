package org.rtm.results;

import org.rtm.struct.Stream;

public class ResultHandler {

	private Stream resultStream = new Stream();

	public void attachResult(AggregationResult r) {
		/* TODO: fix
		 dimensionData = resultStream.get(r.getDimension());
		if(dimensionData == null)
			dimensionData = new ConcurrentHashMap<>();
		dimensionData.put(r.getIntervalBegin(), r.getPayload());
		*/
	}

	public Stream getStreamHandle() {
		return null;
	}
	
}
