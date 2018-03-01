package org.rtm.stream;

public class FinalDimension  extends Dimension<String, Object>{
	private static final long serialVersionUID = 5989391368060961616L;
	//private static final Logger logger = LoggerFactory.getLogger(Dimension.class);

	public FinalDimension(String name){
		super(name);
	}

	public FinalDimension diff(FinalDimension dim2) throws Exception {
		throw new RuntimeException("Needs to be re implemented");
	}

}
