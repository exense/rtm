package org.rtm.rest.dashboard;

import java.util.List;

public class Dashboard {

	private String title;
	private String oid;
	private WidgetWrapper widgets;
	private ManagerState mgrstate;
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getOid() {
		return oid;
	}
	public void setOid(String oid) {
		this.oid = oid;
	}
	public WidgetWrapper getWidgets() {
		return widgets;
	}
	public void setWidgets(WidgetWrapper widgets) {
		this.widgets = widgets;
	}
	public ManagerState getMgrstate() {
		return mgrstate;
	}
	public void setMgrstate(ManagerState mgrstate) {
		this.mgrstate = mgrstate;
	}
}
