package org.rtm.requests;

import java.util.List;
import java.util.Properties;

import org.rtm.requests.guiselector.Selector;
import org.rtm.time.LongTimeInterval;

public class AggregationRequest extends Request{
	
	private LongTimeInterval lti;
	private final Properties prop;
	private final List<Selector> sel;
	
	public AggregationRequest(LongTimeInterval dti, List<Selector> sel, Properties prop) {
		super();
		this.lti = dti;
		this.prop = prop;
		this.sel = sel;
	}

	public LongTimeInterval getLongTimeInterval() {
		return lti;
	}

	public void setLongTimeInterval(LongTimeInterval lti) {
		this.lti = lti;
	}

	public List<Selector> getSelectors() {
		return sel;
	}

	public Properties getProperties() {
		return prop;
	}

}
