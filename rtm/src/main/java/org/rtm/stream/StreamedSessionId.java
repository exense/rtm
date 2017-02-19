package org.rtm.stream;

import java.util.UUID;

public class StreamedSessionId {

	private UUID streamedSessionId;
	
	public StreamedSessionId(){
		this.streamedSessionId = UUID.randomUUID();
	}
	
	public String getStreamedSessionId(){
		return streamedSessionId.toString();
	}
	
}
