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
		//logger.debug("Attaching result for bucket " + tv.getStreamPayloadIdentifier().getIdAsTypedObject());
		PayloadIdentifier<Long> id = tv.getStreamPayloadIdentifier();
		/* Check only relevant for MergeAccumulator*/
		//if(stream.get(id) != null)
		//		logger.error("There's already a result for id=" + id.getIdAsTypedObject());
		
		
		if(stream == null){
			logger.error("Can not attach result to null stream. Stream with id " + stream.getId() + " was probably evicted too early.");
		}else{
			try{
				stream.getStreamData().put(id.getIdAsTypedObject(), tv);
			}catch(NullPointerException e){
				//logger.error("Can not attach result to null collection. Stream with id " + stream.getId() + " has probably timed out.");// prettier than : , e);
				//throw new RuntimeException("Propagating error to shutdown tasks.");
				
				throw new RuntimeException("Stream timed out : " + stream.getId());
			}
		}

	}

	public Stream<Long> getStream() {
		return stream;
	}

}
