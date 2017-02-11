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
import java.util.Map;

import org.rtm.commons.Configuration;
import org.rtm.dao.RTMMongoClient;
import org.rtm.dao.Selector;

@SuppressWarnings({"unchecked", "rawtypes"})
public class MeasurementService{

	public MeasurementService(){}
	
	public List<Map<String, Object>> listAllMeasurements() throws Exception{
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		for(Map<String, Object> m : RTMMongoClient.getInstance().selectMeasurements(null, 0, 0, "n.begin"))
			res.add(m);
		
		return res;
	}
	
	public List<Map<String, Object>> selectMeasurements(List<Selector> slt, String orderBy, int skip, int limit) throws Exception{
		List<Map<String, Object>> res = new ArrayList<Map<String, Object>>();
		RTMMongoClient myclient = RTMMongoClient.getInstance();
		Iterable it = myclient.selectMeasurements(slt, skip, limit, orderBy);
		
		boolean debug = Boolean.parseBoolean(Configuration.getInstance().getProperty("rtm.debug"));
		if(debug)
			System.out.println("selector=" + slt + ";" + "skip=" + skip+"; limit=" + limit + "; orderBy=" + orderBy);
		
		long start = System.currentTimeMillis();
		int itcount = 0;
		StringBuilder sb = new StringBuilder();
		for(Object o : it){
			Map<String, Object> m = (Map) o;
			itcount++;
			 if(debug){
				 long now = System.currentTimeMillis();
				 sb.append("iteration " + itcount + "; duration=" + (now - start) + "ms.\n");
				 start = now;
			 }
			
			res.add(m);
		}
		
		if(debug){
			long end = System.currentTimeMillis();
			sb.append("total duration=" + (end - start) + "ms.\n");
			System.out.println(sb.toString());
		}
		
		return res;
	}
}
