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
package org.rtm.stream.result;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.rtm.range.Identifier;
import org.rtm.stream.Dimension;
import org.rtm.stream.PayloadIdentifier;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * @author doriancransac
 *
 */

public class FinalAggregationResult<T> implements AggregationResult<T> {

	private Map<String, Dimension> map;
	private PayloadIdentifier<T> identifier;
	
	public FinalAggregationResult() {
		super();
	}

	public FinalAggregationResult(PayloadIdentifier<T> id){
		this.identifier = new SimplePayloadIdentifier(id.getIdAsTypedObject());
	}
	
	@JsonIgnore
	@Override
	public PayloadIdentifier<T> getStreamPayloadIdentifier() {
		return this.identifier;
	}

	@Override
	public Map<String, Dimension> getDimensionsMap() {
		return map;
	}
	
	@JsonIgnore
	public Map<String, Dimension> getPrettyDimensionsMap() {
		return map;
	}
	
	@Override
	public void setDimensionsMap(Map<String, Dimension> map) {
		this.map = map;
		
	}
	
	private class SimplePayloadIdentifier<T> implements PayloadIdentifier<T>{

		T id;

		public SimplePayloadIdentifier(T id){
			this.id = id;
		}

		@JsonIgnore
		@Override
		public Identifier<T> getId() {
			return this;
		}

		@Override
		public T getIdAsTypedObject() {
			return this.id;
		}

		@Override
		public int compareTo(Identifier<T> o) {
			throw new RuntimeException("Not Implemented.");
		}


	}

}
