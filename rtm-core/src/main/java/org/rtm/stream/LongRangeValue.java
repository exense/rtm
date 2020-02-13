package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rtm.stream.result.AggregationResult;
import org.rtm.stream.result.Identifier;
import org.rtm.stream.result.OrderedIdentifier;

@SuppressWarnings("rawtypes")
public class LongRangeValue implements AggregationResult<Long>{

	private Identifier<Long> streamPayloadIdentifier;
	private ConcurrentHashMap<String, Dimension> dimensionsMap = new ConcurrentHashMap<String, Dimension>();

	public LongRangeValue(){
		
	}
	
	public LongRangeValue(Long streamPayloadIdentifier){
		super();
		this.streamPayloadIdentifier = new OrderedIdentifier<Long>(streamPayloadIdentifier);
	}

	@Override
	public Identifier<Long> getStreamPayloadIdentifier() {
		return (Identifier<Long>) this.streamPayloadIdentifier;
	}

	public void setStreamPayloadIdentifier(Identifier<Long> streamPayloadIdentifier) {
		this.streamPayloadIdentifier = streamPayloadIdentifier;
	}

	public Dimension getDimension(String dimensionName) {
		return this.dimensionsMap.get(dimensionName);
	}

	public void setDimension(Dimension dimension) {
		this.dimensionsMap.put(dimension.getDimensionName(), dimension);
	}

	@Override
	public Map<String, Dimension> getDimensionsMap() {
		return this.dimensionsMap;
	}
	
	@Override
	public void setDimensionsMap(Map<String, Dimension> map) {
		this.dimensionsMap.clear();
		map.entrySet().stream().forEach(e -> {
			this.dimensionsMap.put(e.getKey(), e.getValue());
		});
	}
}
