package org.rtm.rest.dashboard;

import java.util.List;

public class Dashboard {

	private String title;
	private String dashboardid;
	private List<Object> widgets;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getDashboardid() {
		return dashboardid;
	}
	public void setDashboardid(String dashboardid) {
		this.dashboardid = dashboardid;
	}
	public List<Object> getWidgets() {
		return widgets;
	}
	public void setWidgets(List<Object> widgets) {
		this.widgets = widgets;
	}
}
