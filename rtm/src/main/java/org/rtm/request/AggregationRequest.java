package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.request.selection.Selector;
import org.rtm.time.LongTimeInterval;

public class AggregationRequest extends Request{
	
	private LongTimeInterval timeWindow;
	private Long intervalSize;
	private String primaryDimensionKey;
	private final List<Selector> selectors;
	
	private final Properties properties;
	
	public AggregationRequest(LongTimeInterval timeWindow, List<Selector> selectors, Properties properties) {
		super();
		this.timeWindow = timeWindow;
		this.properties = properties;
		this.selectors = selectors;
	}

	public LongTimeInterval getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(LongTimeInterval timeWindow) {
		this.timeWindow = timeWindow;
	}

	public Long getIntervalSize() {
		return intervalSize;
	}

	public void setIntervalSize(Long intervalSize) {
		this.intervalSize = intervalSize;
	}

	public String getPrimaryDimensionKey() {
		return primaryDimensionKey;
	}

	public void setPrimaryDimensionKey(String primaryDimensionKey) {
		this.primaryDimensionKey = primaryDimensionKey;
	}

	public List<Selector> getSelectors() {
		return selectors;
	}

	public Properties getProperties() {
		return properties;
	}


}
