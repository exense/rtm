package org.rtm.stream;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentMap;

public class StreamedSessionManager {

	private Map<StreamedSessionId, ConcurrentMap> streamRegistry;
	
	public StreamedSessionManager(){
		streamRegistry = new HashMap<>();
	}
	
	public StreamedSessionId registerStreamSession(ConcurrentMap streamHandle) {
		StreamedSessionId id = new StreamedSessionId();
		streamRegistry.put(id, streamHandle);
		return id;
	}

}
