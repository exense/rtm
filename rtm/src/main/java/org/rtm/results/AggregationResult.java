package org.rtm.results;

import java.util.Map;

public class AggregationResult {
	
	private Long intervalBegin;
	private Map<String, Object> payload;
	private String dimension;

	public AggregationResult(Long intervalBegin, Map<String, Object> payload, String dimension) {
		super();
		this.intervalBegin = intervalBegin;
		this.payload = payload;
		this.dimension = dimension;
	}
	
	public Long getIntervalBegin() {
		return intervalBegin;
	}

	public void setIntervalBegin(Long intervalBegin) {
		this.intervalBegin = intervalBegin;
	}

	public Map<String, Object> getPayload() {
		return payload;
	}

	public void setPayload(Map<String, Object> payload) {
		this.payload = payload;
	}

	public String getDimension() {
		return dimension;
	}

	public void setDimension(String dimension) {
		this.dimension = dimension;
	}

	
}
