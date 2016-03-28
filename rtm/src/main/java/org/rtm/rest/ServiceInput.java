package org.rtm.rest;

import java.util.List;
import java.util.Map;

import org.rtm.dao.Selector;

public class ServiceInput {
	
	private List<Selector> selectors; 
	private Map<String, String> serviceParams;
	
	public List<Selector> getSelectors() {
		return selectors;
	}
	public void setSelectors(List<Selector> selectors) {
		this.selectors = selectors;
	}
	public Map<String, String> getServiceParams() {
		return serviceParams;
	}
	public void setServiceParams(Map<String, String> serviceParams) {
		this.serviceParams = serviceParams;
	}

}
