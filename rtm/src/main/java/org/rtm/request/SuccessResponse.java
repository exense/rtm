package org.rtm.request;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SuccessResponse extends AbstractResponse {

	//private static final Logger logger = LoggerFactory.getLogger(AggregationResponse.class);

	public SuccessResponse(Object payload, String message) throws JsonProcessingException{
		super();
		super.setPayload(payload);
		super.setMetaMessage(message);
		super.setStatus(ResponseStatus.SUCCESS);
	}
	

}
