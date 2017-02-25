package org.rtm.stream;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.rtm.time.Identifier;

public class LongRangeValue extends ConcurrentHashMap<String, Dimension> implements AggregationResult<Long>{

	private static final long serialVersionUID = -2891193441467345217L;

	private Identifier<Long> ti;

	public LongRangeValue(Identifier<Long> ti){
		super();
		this.ti = ti;
	}

	@Override
	public PayloadIdentifier<Long> getStreamPayloadIdentifier() {
		return (PayloadIdentifier<Long>) new TimeBasedPayloadIdentifier();
	}


	public Dimension getDimension(String dimensionName) {
		return this.get(dimensionName);
	}

	public void setDimension(Dimension dimension) {
		this.put(dimension.getDimensionValue(), dimension);
	}

	private class TimeBasedPayloadIdentifier implements PayloadIdentifier<Long>{

		Long id;

		public TimeBasedPayloadIdentifier(){
			this.id = (Long)ti.getIdAsTypedObject();
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
		public int compareTo(Long o) {
			return this.id.compareTo(o);
		}

	}

	@SuppressWarnings("rawtypes")
	@Override
	public Map getData() {
		return this;
	}
}
