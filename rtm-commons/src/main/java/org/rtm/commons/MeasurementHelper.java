package org.rtm.commons;

import java.util.ArrayList;
import java.util.List;

public class MeasurementHelper {
	
	public static List<Long> getDurationsList(List<Measurement> l, String durationKey){
		List<Long> res = new ArrayList<Long>();
		for(Measurement t : l)
			res.add(t.getNumericalAttribute(durationKey));
		
		return res;
	}


}
