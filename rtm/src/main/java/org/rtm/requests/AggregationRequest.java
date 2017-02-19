package org.rtm.requests;

import java.util.List;
import java.util.Properties;

import org.rtm.core.DateTimeInterval;
import org.rtm.requests.guiselector.Selector;

public class AggregationRequest extends Request{
	
	private DateTimeInterval dti;
	private final Properties prop;
	private final List<Selector> sel;
	
	public AggregationRequest(DateTimeInterval dti, List<Selector> sel, Properties prop) {
		super();
		this.dti = dti;
		this.prop = prop;
		this.sel = sel;
	}

	public DateTimeInterval getDateInterval() {
		return dti;
	}

	public void setDti(DateTimeInterval dti) {
		this.dti = dti;
	}

	public List<Selector> getSelectors() {
		return sel;
	}

	public Properties getProperties() {
		return prop;
	}

}
