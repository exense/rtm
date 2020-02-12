package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rtm.commons.Identifier;
import org.rtm.commons.OrderedIdentifier;
import org.rtm.stream.result.AggregationResult;

@SuppressWarnings("rawtypes")
public class LongRangeValue extends ConcurrentHashMap<String, Dimension> implements AggregationResult<Long>{

	private static final long serialVersionUID = -2891193441467345217L;

	private OrderedIdentifier<Long> ti;

	public LongRangeValue(Long id){
		super();
		this.ti = new OrderedIdentifier<Long>(id);
	}

	@Override
	public Identifier<Long> getStreamPayloadIdentifier() {
		return (Identifier<Long>) this.ti;
	}


	public Dimension getDimension(String dimensionName) {
		return this.get(dimensionName);
	}

	public void setDimension(Dimension dimension) {
		this.put(dimension.getDimensionName(), dimension);
	}
	

	@Override
	public Map<String, Dimension> getDimensionsMap() {
		return this;
	}
	
	@Override
	public void setDimensionsMap(Map<String, Dimension> map) {
		this.clear();
		map.entrySet().stream().forEach(e -> {
			this.put(e.getKey(), e.getValue());
		});
	}
}
