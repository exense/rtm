/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.request.selection;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Selector {

	private List<TextFilter> textFilters = new ArrayList<TextFilter>();
	private List<NumericalFilter> numericalFilters = new ArrayList<NumericalFilter>();

	@JsonCreator
	public Selector(@JsonProperty("textFilters")List<TextFilter> textFilters,@JsonProperty("numericalFilters") List<NumericalFilter> numericalFilters){
		this.textFilters = textFilters;
		this.numericalFilters = numericalFilters;
	}
	
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
