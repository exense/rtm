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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SuppressWarnings({ "unchecked", "rawtypes" })
public class BlankAggregate extends HashMap {
	
	private static final long serialVersionUID = -2882386371717674686L;
	
	public BlankAggregate() throws Exception{
		super();
		
		put("avg", 0L);
		put("cnt", 0L);
		put("sum", 0L);
		put("min", 0L);
		put("max", 0L);
		put("pcl1", 0L);
		put("pcl10", 0L);
		put("pcl20", 0L);
		put("pcl30", 0L);
		put("pcl40", 0L);
		put("pcl50", 0L);
		put("pcl60", 0L);
		put("pcl70", 0L);
		put("pcl80", 0L);
		put("pcl90", 0L);
		put("pcl99", 0L);
		put("std", 0L);
		
	}

	public static List<Map> trimBlanks(List<Map> value) {

		List<Map> result = new ArrayList<Map>();
		
		for(Map a : value)
			if(!(a instanceof BlankAggregate))
				result.add(a);
		
		return result;
	}

}
