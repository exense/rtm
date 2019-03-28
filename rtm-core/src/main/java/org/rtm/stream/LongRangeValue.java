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
	
	public void setStreamPayloadIdentifier(PayloadIdentifier<Long> streamPayloadIdentifier) {
		this.streamPayloadIdentifier = streamPayloadIdentifier;
	}
	
	public Dimension getDimension(String dimensionName) {
		return dimensionMap.get(dimensionName);
	}

	public void setDimension(Dimension dimension) {
		dimensionMap.put(dimension.getDimensionName(), dimension);
	}


	@Override
	public Map<String, Dimension> getDimensionsMap() {
		return dimensionMap;
	}

	@Override
	public void setDimensionsMap(Map<String, Dimension> map) {
		dimensionMap = map;
	}

	public static class TimeBasedPayloadIdentifier implements PayloadIdentifier<Long>{

		protected Long longId;

		public TimeBasedPayloadIdentifier() {
			super();
		}

		public TimeBasedPayloadIdentifier(Long id){
			this.longId = id;
		}

		@Override
		@JsonIgnore
		public Identifier<Long> getId() {
			return this;
		}

		public void setLongId(long id) {
			this.longId = id;
		}
		
		public long getLongId() {
			return longId;
		}

		@Override
		@JsonIgnore
		public Long getIdAsTypedObject() {
			return this.longId;
		}

		@Override
		public int compareTo(Identifier<Long> o) {
			return this.longId.compareTo(o.getIdAsTypedObject());
		}

	}
}
