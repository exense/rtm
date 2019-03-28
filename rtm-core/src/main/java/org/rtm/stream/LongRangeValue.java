package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rtm.range.Identifier;
import org.rtm.stream.result.AggregationResult;

import com.fasterxml.jackson.annotation.JsonIgnore;

@SuppressWarnings("rawtypes")
public class LongRangeValue implements AggregationResult<Long>{

	private static final long serialVersionUID = -2891193441467345217L;

	private Identifier<Long> streamPayloadIdentifier;

	private Map<String, Dimension> dimensionMap = new ConcurrentHashMap<String, Dimension>();
	
	public LongRangeValue(){}

	public LongRangeValue(Long id){
		super();
		this.streamPayloadIdentifier = new TimeBasedPayloadIdentifier(id);
	}

	@Override
	public PayloadIdentifier<Long> getStreamPayloadIdentifier() {
		return (PayloadIdentifier<Long>) this.streamPayloadIdentifier;
	}
	
	public void setStreamPayloadIdentifier(Long id) {
		this.streamPayloadIdentifier = new TimeBasedPayloadIdentifier(id);
	}
	
	public Dimension getDimension(String dimensionName) {
		return dimensionMap.get(dimensionName);
	}

	public void setDimension(Dimension dimension) {
		dimensionMap.put(dimension.getDimensionName(), dimension);
	}


	@Override
	@JsonIgnore
	public Map<String, Dimension> getDimensionsMap() {
		return dimensionMap;
	}

	@Override
	@JsonIgnore
	public void setDimensionsMap(Map<String, Dimension> map) {
		dimensionMap = map;
	}

	private class TimeBasedPayloadIdentifier implements PayloadIdentifier<Long>{

		Long id;

		public TimeBasedPayloadIdentifier(Long id){
			this.id = id;
		}

		@Override
		public Identifier<Long> getId() {
			return this;
		}

		@Override
		public Long getIdAsTypedObject() {
			return this.id;
		}

		@Override
		public int compareTo(Identifier<Long> o) {
			return this.id.compareTo(o.getIdAsTypedObject());
		}

	}
}
