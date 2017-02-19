package org.rtm.stream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.rtm.struct.Stream;

@SuppressWarnings("rawtypes")
public class StreamedSessionManager {
	/*
	 * streamRregistry(ConcurrentMap<StreamedSessionId, _stream_>)
	 * 			-> stream(ConcurrentMap<Long, _time_>)
	 *					-> time(ConcurrentMap<String, _dimension_>)
	 *							-> dimension(Map<String, _data_>)
	 * 									-> data(Map<String, Object>)
	 * 											-> key, value (String or Long)
	 * 
	 * 
	 */
	private ConcurrentMap<StreamedSessionId, Stream> streamRegistry;
	
	public StreamedSessionManager(){
		streamRegistry = new ConcurrentHashMap<>();
	}
	
	public StreamedSessionId registerStreamSession(Stream streamHandle) {
		StreamedSessionId id = new StreamedSessionId();
		streamRegistry.put(id, streamHandle);
		return id;
	}

}
