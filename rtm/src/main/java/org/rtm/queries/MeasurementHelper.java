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
package org.rtm.queries;

import java.util.Map;
import java.util.Properties;

import org.rtm.commons.Configuration;

/**
 * @author doriancransac
 *
 */
@SuppressWarnings("rawtypes")
public class MeasurementHelper{
	private Configuration conf;
	private String primaryDimension;
	
	public MeasurementHelper(Properties p){
		this.conf = Configuration.getInstance();
		//TODO: this.primaryDimension = p.getProperty(conf.getProperty("properties.primaryDimension.key"))
		this.primaryDimension = "name";
	}
	
	public String getPrimaryDimensionName(Properties prop){
		return "name";
	}

	public String getPrimaryDimensionValue(Properties prop, Map m){
		return (String)m.get(this.primaryDimension);
	}
	
}
