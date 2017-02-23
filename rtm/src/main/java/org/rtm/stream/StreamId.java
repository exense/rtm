package org.rtm.stream;

import java.util.UUID;

public class StreamId {

	private UUID streamedSessionId;
	
	public StreamId(){
		this.streamedSessionId = UUID.randomUUID();
	}
	
	public String getStreamedSessionId(){
		return streamedSessionId.toString();
	}
	
}
