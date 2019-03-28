package org.rtm.stream;

import org.rtm.metrics.WorkObject;

public class WorkDimension  extends Dimension<String, WorkObject>{

	public WorkDimension(){}
	
	public WorkDimension(String name){
		super(name);
	}

	public WorkDimension diff(WorkDimension dim2) throws Exception {
		throw new RuntimeException("Not implemented");
	}
}
