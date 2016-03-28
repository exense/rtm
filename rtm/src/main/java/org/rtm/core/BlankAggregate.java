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
