package org.rtm.stream.result;

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

		if(tv == null){
			//Something went wrong during the aggregation, nothing we can do here
			return;
		}
		
		Identifier<Long> id = tv.getStreamPayloadIdentifier();
		
		if(stream == null){
			logger.error("Can not attach result to null stream. Stream with id " + stream.getId() + " was probably evicted too early.");
		}else{
			if(stream == null || stream.getStreamData() == null)
				throw new RuntimeException("Null Stream or Stream Data. Stream may have timed out : " + stream.getId());
			else
				stream.getStreamData().put(id.getIdAsTypedObject(), tv);
		}
	}

	public Stream<Long> getStream() {
		return stream;
	}

}
