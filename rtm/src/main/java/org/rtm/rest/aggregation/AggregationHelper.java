package org.rtm.rest.aggregation;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class AggregationHelper {
	
	public static List<Long> getDurationsList(List<Map<String, Object>> l, String durationKey) throws Exception{
		List<Long> res = new ArrayList<Long>();
		for(Map<String, Object> m : l){
			Object obj = m.get(durationKey);
			
			if(obj instanceof Integer)
				res.add(new Long((Integer)obj));
			else if(obj instanceof Long || obj instanceof Double)
					res.add((Long)obj);
			else
				throw new Exception("Inproper numerical value:" + obj.toString());
		}
		
		return res;
	}


}
