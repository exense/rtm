package org.rtm.stream;

import java.util.UUID;

public class StreamedSessionId {

	private UUID id;
	
	public StreamedSessionId(){
		this.id = UUID.randomUUID();
	}
	
	public String getIdentifierAsString(){
		return id.toString();
	}
	
}
