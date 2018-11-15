package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.range.time.LongTimeInterval;
import org.rtm.selection.Selector;

public class AggregationRequest extends Request{
	
	private LongTimeInterval timeWindow1;
	private List<Selector> selectors1;
	//private Properties properties;
	private Properties serviceParams;
	
	public AggregationRequest() {
		super();
	}
	
	public AggregationRequest(LongTimeInterval timeWindow1, List<Selector> selectors1, Properties serviceParams) {
		super();
		this.timeWindow1 = timeWindow1;
		this.serviceParams = serviceParams;
		this.selectors1 = selectors1;
	}

	public LongTimeInterval getTimeWindow1() {
		return timeWindow1;
	}

	public void setTimeWindow(LongTimeInterval timeWindow) {
		this.timeWindow1 = timeWindow;
	}

	public List<Selector> getSelectors1() {
		return selectors1;
	}

	public Properties getServiceParams() {
		return serviceParams;
	}

	public String toString(){
		return "timeWindow={" + this.timeWindow1 + "}, selectors={" + this.selectors1 + "}, serviceParams={" + this.serviceParams + "}";
	}

}
