package org.rtm.stream.result;

import org.rtm.stream.AggregationResult;
import org.rtm.stream.PayloadIdentifier;
import org.rtm.stream.Stream;
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
			logger.error("There's already a result for id=" + id.getIdAsTypedObject());
		stream.put(id.getIdAsTypedObject(), tv);

	}

	public Stream<Long> getStream() {
		return stream;
	}

}
