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
package org.rtm.rest.aggregation;

import java.util.ArrayList;
import java.util.List;

import org.rtm.core.AggregateResult;

public class AggOutput {
	
	private List<AggregateResult> payload;
	private String warning;
	
	public List<AggregateResult> getPayload() {
		return payload;
	}
	public void setPayload(List<AggregateResult> payload) {
		this.payload = payload;
	}
	public String getWarning() {
		return warning;
	}
	public void setWarning(String warning) {
		this.warning = warning;
	}
	public void setShallowPayload() {
		this.payload = new ArrayList<AggregateResult>();		
	}

	public String toString(){
		return "payload="+payload.toString()+"; warning=" + warning + ";";
	}
}
