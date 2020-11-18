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
package org.rtm.measurement;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.rtm.request.selection.NumericalFilter;
import org.rtm.request.selection.Selector;
import org.rtm.request.selection.TextFilter;

@SuppressWarnings("rawtypes")
public class MeasurementHelper{
	private String dimensionDirective;

	private List<String> splitDimensions;
	
	private static Character splitChar = ';'; 
	
	public MeasurementHelper(Properties p){
		this.dimensionDirective = p.getProperty("aggregateService.groupby");
		this.splitDimensions = Arrays.asList(this.dimensionDirective.split(splitChar.toString()));
	}

	public List<String> getSplitDimensions() {
		return splitDimensions;
	}
	
	public String getDimensionDirectiveName(){
		return this.dimensionDirective;
	}

	public String getActualDimensionName(Map m){
		StringBuilder dims = new StringBuilder();
		for(String dim : this.splitDimensions){
			String asString = "default";
			Object actual = m.get(dim);
			
			if(actual != null)
				asString = actual.toString();
			
			dims.append(asString).append(this.splitChar);
		}
		dims.setLength(dims.length() - 1);
		return dims.toString();
	}

	public List<Selector> getDimensionSelectors(List<Selector> sel, Map m) {
		//Create the filter for the current dimension
		List<TextFilter> dimFilters = new ArrayList<TextFilter> ();
		for(String dim : this.splitDimensions) {
			String asString = "default";
			Object actual = m.get(dim);
			if(actual != null)
				asString = actual.toString();
			dimFilters.add(new TextFilter(false,dim,asString));
		}
		//Copy the existing selectors
		List<Selector> selList = new ArrayList<Selector> ();
		sel.forEach(s-> {
			Selector sCopy = new Selector();
			List<TextFilter> textFilters = sCopy.getTextFilters();
			List<NumericalFilter> numericalFilters = sCopy.getNumericalFilters();
			s.getTextFilters().forEach(t->textFilters.add(new TextFilter(t.isRegex(),t.getKey(),t.getValue())));
			s.getNumericalFilters().forEach(n->numericalFilters.add(new NumericalFilter(n.getKey(),n.getMinValue(),n.getMaxValue())));
			selList.add(sCopy);
		});
		//Add dim filters to all selectors
		if (selList.size()>0) {
			selList.forEach(s->dimFilters.forEach(f->s.addTextFilter(f)));
		} else {
			Selector selector = new Selector();
			dimFilters.forEach(f->selector.addTextFilter(f));
			selList.add(selector);
		}
		return selList;
		
	}
	
}
