package org.rtm.stream;

import java.util.Properties;
import java.util.concurrent.ConcurrentSkipListMap;

import ch.exense.commons.app.Configuration;
import org.rtm.stream.result.AggregationResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

/*
 * Wraps a CSLM which contains an ordered representation of the entire timeseries data of the stream 
 */
public class Stream<T> {
	
	private static final Logger logger = LoggerFactory.getLogger(Stream.class);

	public static final String INTERVAL_SIZE_KEY = "intervalSize";
	
	private ConcurrentSkipListMap<Long, AggregationResult<T>> streamData;
	private StreamId id = new StreamId(); 
	private boolean isClone = false; 
	
	private long timeCreated = System.currentTimeMillis();
	private long timeEnded = 0;
	private long timeoutDurationSecs;
	
	private boolean complete = false;
	private boolean isRefreshedSinceCompletion = false;
	
	@JsonIgnore
	boolean isCompositeStream = false;

	@JsonIgnore
	private Properties streamProp; // pass stream-specific concrete information? currently unused
	
	public Stream(Properties prop, Configuration configuration){
		this.streamProp = prop;
		this.setStreamData(new ConcurrentSkipListMap<Long, AggregationResult<T>>());
		try {
			this.setTimeoutDurationSecs(configuration.getPropertyAsInteger("aggregateService.defaultStreamTimeoutSecs"));
		} catch (Exception e) {
			logger.error("Could not access default timeout value.", e);
			this.setTimeoutDurationSecs(600);
		}
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
		this.timeEnded = System.currentTimeMillis();
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
	
	public void closeStream(){
		this.streamData.clear();
		this.streamData = null;
	}

	@JsonIgnore
	public long getTimeoutDurationSecs() {
		return timeoutDurationSecs;
	}

	@JsonIgnore
	public void setTimeoutDurationSecs(long timeoutDurationSecs) {
		this.timeoutDurationSecs = timeoutDurationSecs;
	}

	public StreamId getId() {
		return id;
	}

	public boolean isClone() {
		return isClone;
	}
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		this.streamData.entrySet().stream().forEach(e -> {
			sb.append("{"); sb.append(e.getKey());sb.append("=");sb.append(e.getValue().toString());
		});
		return sb.toString();
	}

	public Properties getStreamProp() {
		return streamProp;
	}

	public void setStreamProp(Properties streamProp) {
		this.streamProp = streamProp;
	}
	
	public long getDurationMs() {
		if(this.timeEnded > 0)
			return this.timeEnded - this.timeCreated;
		else
			return System.currentTimeMillis() - this.timeCreated;
	}
	
	public boolean isCompositeStream() {
		return isCompositeStream;
	}

	public void setCompositeStream(boolean isCompositeStream) {
		this.isCompositeStream = isCompositeStream;
	}
}
