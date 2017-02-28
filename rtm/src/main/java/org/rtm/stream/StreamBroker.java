package org.rtm.stream;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@SuppressWarnings("rawtypes")
public class StreamBroker {
	/*
	 * streamRegistry(ConcurrentMap<StreamedSessionId, _stream_>)
	 * 			-> stream(ConcurrentMap<Long, _time_>)
	 *					-> time(ConcurrentMap<String, _dimension_>)
	 *							-> dimension(Map<String, _data_>)
	 * 									-> data(Map<String, Object>)
	 * 											-> key, value (String or Long)
	 * 
	 * 
	 */
	private ConcurrentMap<StreamId, Stream> streamRegistry;
	
	public StreamBroker(){
		streamRegistry = new ConcurrentHashMap<>();
	}
	
	public StreamId registerStreamSession(Stream streamHandle) {
		StreamId id = new StreamId();
		streamRegistry.put(id, streamHandle);
		return id;
	}
	
	public Stream getStream(StreamId id){
		return this.streamRegistry.get(id);
	}

}
