package org.rtm.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamResultHandler<T> implements TimebasedResultHandler<T>{

	private static final Logger logger = LoggerFactory.getLogger(StreamResultHandler.class);
	
	private final Stream<T> stream;
	
	public StreamResultHandler(Stream<T> stream){
		this.stream = stream;
	}

	public void attachResult(AggregationResult<T> tv) {
		PayloadIdentifier<T> id = tv.getStreamPayloadIdentifier();
		if(stream.get(id) != null)
			logger.warn("There's already a result for id=" + id);
		stream.put(id, tv);
	}

	public Stream<T> getStream() {
		return stream;
	}

	
}
