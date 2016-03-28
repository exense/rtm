package org.rtm.dao;

import java.util.ArrayList;
import java.util.List;

public class Selector {
	
	private List<TextFilter> textFilters = new ArrayList<TextFilter>();
	private List<NumericalFilter> numericalFilters = new ArrayList<NumericalFilter>();
	
	public Selector(){}
	
	public List<TextFilter> getTextFilters() {
		return textFilters;
	}
	public void addTextFilter(TextFilter textFilter) {
		textFilter.setKey(textFilter.getKey());
		this.textFilters.add(textFilter);
	}
	public boolean hasTextFilters() {
		if(textFilters != null && textFilters.size() > 0)
			return true;
		return false;
	}
	
	public List<NumericalFilter> getNumericalFilters() {
		return numericalFilters;
	}
	public void addNumericalFilter(NumericalFilter numericalFilter) {
		numericalFilter.setKey(numericalFilter.getKey());
		this.numericalFilters.add(numericalFilter);
	}
	public boolean hasNumericalFilters() {
		if(numericalFilters != null && numericalFilters.size() > 0)
			return true;
		return false;
	}
	public String toString(){
		return "{"+this.getTextFilters().toString()+"},{"+this.getNumericalFilters().toString()+"}";
	}
}
