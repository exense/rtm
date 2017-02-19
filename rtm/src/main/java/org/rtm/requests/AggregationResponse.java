package org.rtm.requests;

import org.rtm.stream.StreamedSessionId;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.core.JsonProcessingException;

public class AggregationResponse extends AbstractResponse {

	//private static final Logger logger = LoggerFactory.getLogger(AggregationResponse.class);

	public AggregationResponse(StreamedSessionId registeredStreamSessionId) throws JsonProcessingException{
		super.setPayload(registeredStreamSessionId);
		super.setMetaMessage("Stream initialized. Call the streaming service next to start retrieving data.");
		super.setStatus(ResponseStatus.SUCCESS);
	}
	
	@JsonIgnore
	public StreamedSessionId getsId() {
		return (StreamedSessionId) super.getPayload();
	}

}
