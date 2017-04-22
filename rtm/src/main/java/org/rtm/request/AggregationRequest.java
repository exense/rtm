package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.selection.Selector;

public class AggregationRequest extends Request{
	
	private LongTimeInterval timeWindow;
	private List<Selector> selectors;
	//private Properties properties;
	private Properties serviceParams;
	
	public AggregationRequest() {
		super();
	}
	
	public AggregationRequest(LongTimeInterval timeWindow, List<Selector> selectors, Properties serviceParams) {
		super();
		this.timeWindow = timeWindow;
		this.serviceParams = serviceParams;
		this.selectors = selectors;
	}

	public LongTimeInterval getTimeWindow() {
		return timeWindow;
	}

	public void setTimeWindow(LongTimeInterval timeWindow) {
		this.timeWindow = timeWindow;
	}

	public List<Selector> getSelectors() {
		return selectors;
	}

	public Properties getServiceParams() {
		return serviceParams;
	}


}
