package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.range.time.LongTimeInterval;
import org.rtm.selection.Selector;

public class ComparisonRequest extends Request{
	
	private LongTimeInterval timeWindow1;
	private LongTimeInterval timeWindow2;
	
	private List<Selector> selectors1;
	private List<Selector> selectors2;
	//private Properties properties;
	private Properties serviceParams;
	
	public ComparisonRequest() {
		super();
	}
	
	public ComparisonRequest(LongTimeInterval timeWindow1, LongTimeInterval timeWindow2, List<Selector> selectors1, List<Selector> selectors2, Properties serviceParams) {
		super();
		this.timeWindow1 = timeWindow1;
		this.timeWindow2 = timeWindow2;
		this.serviceParams = serviceParams;
		this.selectors1 = selectors1;
		this.selectors2 = selectors2;
	}

	public LongTimeInterval getTimeWindow1() {
		return timeWindow1;
	}
	
	public LongTimeInterval getTimeWindow2() {
		return timeWindow2;
	}

	public void setTimeWindow1(LongTimeInterval timeWindow1) {
		this.timeWindow1 = timeWindow1;
	}

	public void setTimeWindow2(LongTimeInterval timeWindow2) {
		this.timeWindow2 = timeWindow2;
	}

	public List<Selector> getSelectors1() {
		return selectors1;
	}

	public List<Selector> getSelectors2() {
		return selectors2;
	}

	public Properties getServiceParams() {
		return serviceParams;
	}

	public String toString(){
		return "timeWindow1={" + this.timeWindow1 + "}, timeWindow2={" + this.timeWindow2 + "}, selectors1={" + this.selectors1 + "}, selectors2={" + this.selectors1 + "}, serviceParams={" + this.serviceParams + "}";
	}


}
