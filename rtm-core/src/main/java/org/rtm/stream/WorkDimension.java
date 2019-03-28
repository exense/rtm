package org.rtm.stream;

import org.rtm.metrics.WorkObject;
import org.rtm.serialization.WorkDimensionSerializer;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

public class WorkDimension  extends Dimension<String, WorkObject>{
	private static final long serialVersionUID = 5989391368060961616L;
	//private static final Logger logger = LoggerFactory.getLogger(Dimension.class);

	public WorkDimension(){}
	
	public WorkDimension(String name){
		super(name);
	}

	public WorkDimension diff(WorkDimension dim2) throws Exception {
		throw new RuntimeException("Not implemented");
	}

	public WorkObject put(String key, WorkObject value) {
		return super.put(key, value);
	}
	
	public WorkObject get(String key) {
		try{
			Object o = super.get(key);
		return (WorkObject)o;
		}catch(ClassCastException e){
			System.out.println(e);
			return null;
		}
	}
}
