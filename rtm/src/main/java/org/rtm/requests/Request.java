package org.rtm.requests;

import java.util.List;
import java.util.Properties;

import org.rtm.core.DateTimeInterval;
import org.rtm.dao.Selector;

public class Request {
	
	private DateTimeInterval dti;
	private final Properties prop;
	
	public Request(DateTimeInterval dti, Properties prop) {
		super();
		this.dti = dti;
		this.prop = prop;
	}

	public DateTimeInterval getDateInterval() {
		return dti;
	}

	public void setDti(DateTimeInterval dti) {
		this.dti = dti;
	}

	public List<Selector> getSelectors() {
		// TODO Auto-generated method stub
		return null;
	}

	public Properties getProperties() {
		return prop;
	}

}
