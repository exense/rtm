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
package org.rtm.core;

import java.util.List;
import java.util.Map;

public class AggregateResult {

	private String groupby;
	private List<Map<String,Object>> aggregates;
	
	public String getGroupby() {
		return groupby;
	}
	public void setGroupby(String groupby) {
		this.groupby = groupby;
	}
	public List<Map<String, Object>> getData() {
		return aggregates;
	}
	public void setData(List<Map<String, Object>> data) {
		this.aggregates = data;
	}
	public String toString(){
		return "groupby="+groupby + "; size=" + aggregates.size() +"; aggregates=" + aggregates;
	}
	
}
