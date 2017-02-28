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

import org.rtm.range.Identifier;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

public class AccumulationContext extends LongRangeValue{

	private static final long serialVersionUID = -1234819766224267981L;
	
	public AccumulationContext(Identifier<Long> id){
		super(id);
	}
	
	public Dimension getAccHelperForDimension(String dimensionName){
		return super.getDimension(dimensionName);
	}
	
	public void outerMerge(){
		this.values().stream().forEach(d -> {
			d.copyAndFlush();
		});
	} 
	
}
