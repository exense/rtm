package org.rtm.stream;

public class FinalDimension  extends Dimension<String, Object>{

	public FinalDimension(String name){
		super(name);
	}

	public FinalDimension() {
		super();
	}

	public FinalDimension diff(FinalDimension dim2) throws Exception {
		FinalDimension diff = new FinalDimension(this.getDimensionName());
		
		this.map.entrySet().stream().forEach(e -> {
			String metricName = e.getKey();
			Object val2 = dim2.get(metricName);
			Object val1 = this.get(metricName);
			if(val2 instanceof Float)
				diff.put(metricName, (Float)val2 - (Float)val1);
			else
				if(val2 instanceof Long)
					diff.put(metricName, (Long)val2 - (Long)val1);
				else
					throw new RuntimeException("Unsupported metric type for value " + val2);
			
		});
		
		return diff;
	}

}
