package org.rtm.stream;

import org.rtm.metrics.WorkObject;

public class WorkDimension  extends Dimension<String, WorkObject>{
	private static final long serialVersionUID = 5989391368060961616L;
	//private static final Logger logger = LoggerFactory.getLogger(Dimension.class);

	public WorkDimension(String name){
		super(name);
	}

	public WorkDimension diff(WorkDimension dim2) throws Exception {
		throw new RuntimeException("Needs to be re implemented");
	}

	public void put(String key, WorkObject value) {
		super.put(key, value);
	}
	
	public WorkObject get(String key) {
		return (WorkObject)super.get(key);
	}
}
