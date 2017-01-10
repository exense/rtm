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

import org.rtm.commons.Measurement;

public class ComplexServiceResponse {

	public static enum Status{
		NORMAL,
		WARNING
	}
	
	private Map<String,List<Measurement>> payload;
	private Status returnStatus;
	private String message = ";";
	
	public Map<String,List<Measurement>> getPayload() {
		return payload;
	}
	public void setPayload(Map<String,List<Measurement>> payload) {
		this.payload = payload;
	}
	public Status getReturnStatus() {
		return returnStatus;
	}
	public void setReturnStatus(Status returnStatus) {
		this.returnStatus = returnStatus;
	}
	public String getMessage() {
		return message;
	}
	public void setMessage(String message) {
		this.message = message;
	}
	
}
