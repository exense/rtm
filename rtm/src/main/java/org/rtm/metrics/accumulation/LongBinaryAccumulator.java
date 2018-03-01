package org.rtm.metrics.accumulation;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.MapWorkObject;
import org.rtm.metrics.WorkObject;
import org.rtm.range.RangeBucket;

public abstract class LongBinaryAccumulator implements AbstractAccumulator<Long, Long>{

	private LongAccumulator accumulator;
	private static String ACCUMULATOR_KEY = "accumulator";

	public LongAccumulator getAccumulator() {
		return accumulator;
	}

	public void setAccumulator(LongAccumulator accumulator) {
		this.accumulator = accumulator;
	}
	
	@Override
	public void initialize(WorkObject wobj) {
		setAccumulator((LongAccumulator)wobj.getValueObject(ACCUMULATOR_KEY));
	}

	@Override
	public void accumulate(RangeBucket<Long> bucket, Long value) {
		this.accumulator.accumulate(value);
	}
	
	@Override
	public Long getValue() {
		return this.accumulator.get();
	}

	protected class LongAccumulatorWorkObject extends MapWorkObject{
	
		public LongAccumulatorWorkObject(LongBinaryOperator op, Long identity) {
			super();
			super.setValueObject(ACCUMULATOR_KEY, new LongAccumulator(op, identity));
		}
		
	}

}
