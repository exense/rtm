package org.rtm.rest.dashboard;

import java.util.List;

public class Session {
	
	private String name;
	private List<Dashboard> state;
	
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Dashboard> getState() {
		return state;
	}
	public void setState(List<Dashboard> state) {
		this.state = state;
	}


}
