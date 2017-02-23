package org.rtm.request;

import org.rtm.stream.StreamId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AggregationResponse extends AbstractResponse {

	//private static final Logger logger = LoggerFactory.getLogger(AggregationResponse.class);

	public AggregationResponse(StreamId registeredStreamSessionId) throws JsonProcessingException{
		super.setPayload(registeredStreamSessionId);
		super.setMetaMessage("Stream initialized. Call the streaming service next to start retrieving data.");
		super.setStatus(ResponseStatus.SUCCESS);
	}
	
	@JsonIgnore
	public StreamId getsId() {
		return (StreamId) super.getPayload();
	}

}
