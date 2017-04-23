package org.rtm.stream;

import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Wraps a CSLM which contains an ordered representation of the entire timeseries data of the stream 
 */
public class Stream<T> {
	
	private ConcurrentSkipListMap<Long, AggregationResult<T>> streamData;
	
	private long timeCreated = System.currentTimeMillis();
	
	private boolean complete = false;
	private boolean isRefreshedSinceCompletion = false;
	
	public Stream(){
		this.setStreamData(new ConcurrentSkipListMap<Long, AggregationResult<T>>());
	}
	
	@JsonIgnore
	public long getTimeCreated() {
		return timeCreated;
	}

	public synchronized boolean isComplete() {
		return complete;
	}

	public synchronized void setComplete(boolean isComplete) {
		this.complete = isComplete;
	}

	@JsonIgnore
	public synchronized boolean isRefreshedSinceCompletion() {
		return isRefreshedSinceCompletion;
	}

	@JsonIgnore
	public synchronized void setRefreshedSinceCompletion(boolean isRefreshedSinceCompletion) {
		this.isRefreshedSinceCompletion = isRefreshedSinceCompletion;
	}

	public ConcurrentSkipListMap<Long, AggregationResult<T>> getStreamData() {
		return streamData;
	}

	public void setStreamData(ConcurrentSkipListMap<Long, AggregationResult<T>> streamData) {
		this.streamData = streamData;
	}

}
