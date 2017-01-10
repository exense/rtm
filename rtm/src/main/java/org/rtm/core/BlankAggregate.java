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
import java.util.List;

import org.jongo.marshall.jackson.oid.ObjectId;
import org.rtm.commons.Measurement;

public class BlankAggregate extends Measurement {
	
	@ObjectId
	private String _id;
	
	public BlankAggregate() throws Exception{
		super();
		
		setNumericalAttribute("avg", 0L);
		setNumericalAttribute("cnt", 0L);
		setNumericalAttribute("sum", 0L);
		setNumericalAttribute("min", 0L);
		setNumericalAttribute("max", 0L);
		setNumericalAttribute("pcl1", 0L);
		setNumericalAttribute("pcl10", 0L);
		setNumericalAttribute("pcl20", 0L);
		setNumericalAttribute("pcl30", 0L);
		setNumericalAttribute("pcl40", 0L);
		setNumericalAttribute("pcl50", 0L);
		setNumericalAttribute("pcl60", 0L);
		setNumericalAttribute("pcl70", 0L);
		setNumericalAttribute("pcl80", 0L);
		setNumericalAttribute("pcl90", 0L);
		setNumericalAttribute("pcl99", 0L);
		
	}

	public static List<Measurement> trimBlanks(List<Measurement> value) {

		List<Measurement> result = new ArrayList<Measurement>();
		
		for(Measurement a : value)
			if(!(a instanceof BlankAggregate))
				result.add(a);
		
		return result;
	}

}
