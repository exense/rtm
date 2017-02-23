package org.rtm.stream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StreamResultHandler implements ResultHandler<Long>{

	private static final Logger logger = LoggerFactory.getLogger(StreamResultHandler.class);
	
	private final Stream<Long> stream;
	
	public StreamResultHandler(Stream<Long> stream){
		this.stream = stream;
	}

	public void attachResult(AggregationResult<Long> tv) {
		PayloadIdentifier<Long> id = tv.getStreamPayloadIdentifier();
		if(stream.get(id) != null)
			logger.warn("There's already a result for id=" + id);
		stream.put(id.getIdAsTypedObject(), tv);
	}

	public Stream<Long> getStream() {
		return stream;
	}

}
