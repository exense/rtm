package org.rtm.stream;

import java.util.concurrent.ConcurrentSkipListMap;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Wraps a CSLM which contains an ordered representation of the entire timeseries data of the stream 
 */
public class Stream<T> extends ConcurrentSkipListMap<Long, AggregationResult<T>>{

	private static final long serialVersionUID = 8431902330094695624L;
	
	@JsonIgnore
	private long timeCreated = System.currentTimeMillis();
	
	private boolean complete = false;
	
	@JsonIgnore
	private boolean isRefreshedSinceCompletion = false;
	
	public Stream(){
		super();
	}
	
	public long getTimeCreated() {
		return timeCreated;
	}

	public synchronized boolean isComplete() {
		return complete;
	}

	public synchronized void setComplete(boolean isComplete) {
		this.complete = isComplete;
	}

	public synchronized boolean isRefreshedSinceCompletion() {
		return isRefreshedSinceCompletion;
	}

	public synchronized void setRefreshedSinceCompletion(boolean isRefreshedSinceCompletion) {
		this.isRefreshedSinceCompletion = isRefreshedSinceCompletion;
	}

}
