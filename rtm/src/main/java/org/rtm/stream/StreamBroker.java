package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.Executors;

import org.rtm.commons.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
	private static final Logger logger = LoggerFactory.getLogger(StreamBroker.class);
	
	private ConcurrentMap<String, Stream> streamRegistry;
	
	public StreamBroker(){
		streamRegistry = new ConcurrentHashMap<>();
		long defaultTimeout = 600;
		try {
			defaultTimeout = Configuration.getInstance().getPropertyAsInteger("aggregateService.defaultStreamTimeoutSecs");
		} catch (Exception e) {
			logger.error("Couldn't load timeout value from conf.", e);
		}
		
		Executors.newSingleThreadExecutor().submit(new StreamCleaner(this, defaultTimeout, 5));
	}
	
	public void registerStreamSession(Stream streamHandle) {
		streamRegistry.put(streamHandle.getId().toString(), streamHandle);
	}
	
	public Stream getStream(StreamId id){
		return this.streamRegistry.get(id.getStreamedSessionId());
	}
	
	public Stream getStreamAndFlagForRefresh(StreamId id) throws Exception{
		Stream thisStream = getStream(id);
		if(thisStream == null){
			throw new UnknownStreamException("Stream with id " + id.getStreamedSessionId() + " is null.");
		}
		
		if(thisStream.isComplete())
			thisStream.setRefreshedSinceCompletion(true);
		
		return thisStream;
	}

	public Map<String, Stream> getStreamRegistry(){
		return this.streamRegistry;
	}
}
