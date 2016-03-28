package org.rtm.dao;

public class TextFilter {
	
	private boolean isRegex = false;
	private String key;
	private String value;
	
	public boolean isRegex() {
		return isRegex;
	}
	public void setRegex(boolean isRegex) {
		this.isRegex = isRegex;
	}
	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public String toString(){
		return "["+this.getKey()+":"+this.getValue()+", regex=" + this.isRegex()+"]";
	}
}
