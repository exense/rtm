package org.rtm.rest.dashboard;

import java.util.List;

public class Dashboard {
	
	private String name;
	private List<Object> state;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Object> getState() {
		return state;
	}
	public void setState(List<Object> state) {
		this.state = state;
	}


}
