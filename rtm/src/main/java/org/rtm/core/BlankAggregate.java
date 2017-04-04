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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.rtm.core.MeasurementAggregator.AggregationType;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BlankAggregate extends HashMap {
	
	private static final long serialVersionUID = -2882386371717674686L;
	
	public BlankAggregate() throws Exception{
		super();
		
		for(AggregationType type : Arrays.asList(AggregationType.values()))
			put(type.getShort(), 0L);
		
	}

	public static List<Map> trimBlanks(List<Map> value) {

		List<Map> result = new ArrayList<Map>();
		
		for(Map a : value)
			if(!(a instanceof BlankAggregate))
				result.add(a);
		
		return result;
	}

}