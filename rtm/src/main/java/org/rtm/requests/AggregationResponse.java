package org.rtm.requests;

import org.json.JSONObject;
import org.rtm.stream.StreamedSessionId;

public class AggregationResponse extends AbstractResponse {

	private final StreamedSessionId sId;
	
	public AggregationResponse(StreamedSessionId registeredStreamSessionId) {
		this.sId = registeredStreamSessionId;
		
		super.setPayload(new JSONObject().accumulate("streamId", this.sId.getIdentifierAsString()));
		super.setMetaMessage("Stream initialized. Call the streaming service next to start retrieving data.");
		super.setStatus(ResponseStatus.SUCCESS);
	}

	public StreamedSessionId getsId() {
		return sId;
	}
	
}
