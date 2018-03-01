package org.rtm.metrics.accumulation;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.WorkObject;

public abstract class LongBinaryAccumulator implements Accumulator<Long, Long>{

	public static String ACCUMULATOR_KEY = "LongBinaryAccumulator";

	protected abstract String getConcreteAccumulatorKey();
	
	@Override
	public void accumulate(WorkObject wobj, Long value) {
		((LongBinaryAccumulatorState)wobj.getPayload(getConcreteAccumulatorKey())).getAccumulator().accumulate(value);
	}
	
	@Override
	public Long getValue(WorkObject wobj) {
		return ((LongBinaryAccumulatorState)wobj.getPayload(getConcreteAccumulatorKey())).getAccumulator().get();
	}
	
	@Override
	public void mergeLeft(WorkObject wobj1, WorkObject wobj2) {
		
		LongBinaryAccumulatorState lawobj1 = ((LongBinaryAccumulatorState)wobj1.getPayload(getConcreteAccumulatorKey()));
		LongBinaryAccumulatorState lawobj2 = ((LongBinaryAccumulatorState)wobj2.getPayload(getConcreteAccumulatorKey()));

		lawobj1.setAccumulator(new LongAccumulator(lawobj1.getOperator(),mergeValues(lawobj1.getAccumulator().get(), lawobj2.getAccumulator().get())));
	}
	
	protected abstract Long mergeValues(Long value1, Long value2);

	protected class LongBinaryAccumulatorState implements AccumulatorState{
	
		private LongAccumulator accumulator;
		private LongBinaryOperator operator;
		
		public LongBinaryAccumulatorState(LongBinaryOperator op, Long identity) {
			super();
			accumulator = new LongAccumulator(op, identity);
			operator = op;
		}
		
		public LongAccumulator getAccumulator() {
			return accumulator;
		}

		public void setAccumulator(LongAccumulator accumulator) {
			this.accumulator = accumulator;
		}

		public LongBinaryOperator getOperator() {
			return operator;
		}

		public void setOperator(LongBinaryOperator operator) {
			this.operator = operator;
		}
		
		public String toString(){
			return this.accumulator.toString();
		}
	}
	
}
