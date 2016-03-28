package org.rtm.rest;

import java.util.ArrayList;
import java.util.List;

import org.rtm.core.AggregateResult;

public class ServiceOutput {
	
	private List<AggregateResult> payload;
	private String warning;
	
	public List<AggregateResult> getPayload() {
		return payload;
	}
	public void setPayload(List<AggregateResult> payload) {
		this.payload = payload;
	}
	public String getWarning() {
		return warning;
	}
	public void setWarning(String warning) {
		this.warning = warning;
	}
	public void setShallowPayload() {
		this.payload = new ArrayList<AggregateResult>();		
	}

}
