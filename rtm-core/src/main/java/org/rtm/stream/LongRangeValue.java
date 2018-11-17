package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rtm.range.Identifier;
import org.rtm.serialization.LongRangeValueSerializer;
import org.rtm.stream.result.AggregationResult;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@SuppressWarnings("rawtypes")
@JsonSerialize(using = LongRangeValueSerializer.class)
public class LongRangeValue extends ConcurrentHashMap<String, Dimension> implements AggregationResult<Long>{

	private static final long serialVersionUID = -2891193441467345217L;

	private Identifier<Long> streamPayloadIdentifier;

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
