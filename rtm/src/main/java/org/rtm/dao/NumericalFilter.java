package org.rtm.dao;

public class NumericalFilter {

	private String key;
	private Long minValue;
	private Long maxValue;

	public String getKey() {
		return key;
	}
	public void setKey(String key) {
		this.key = key;
	}
	public Long getMinValue() {
		return minValue;
	}
	public void setMinValue(Long minValue) {
		this.minValue = minValue;
	}
	public boolean hasMinValue() {
		if(minValue == null)
			return false;
		return true;
	}
	public Long getMaxValue() {
		return maxValue;
	}
	public void setMaxValue(Long maxValue) {
		this.maxValue = maxValue;
	}
	public boolean hasMaxValue() {
		if(maxValue == null)
			return false;
		return true;
	}
	public String toString(){
		return "["+this.getKey()+":"+this.getMaxValue()+"," + this.getMinValue()+"]";
	}
}
