package org.rtm.core;

import java.util.List;

import org.rtm.commons.Measurement;

public class AggregateResult {

	private String groupby;
	private List<Measurement> aggregates;
	
	public String getGroupby() {
		return groupby;
	}
	public void setGroupby(String groupby) {
		this.groupby = groupby;
	}
	public List<Measurement> getData() {
		return aggregates;
	}
	public void setData(List<Measurement> data) {
		this.aggregates = data;
	}
	
}
