package org.rtm.metrics.accumulation.base;

import java.util.concurrent.atomic.LongAccumulator;
import java.util.function.LongBinaryOperator;

import org.rtm.metrics.WorkObject;

import com.fasterxml.jackson.annotation.JsonIgnore;


public class LongBinaryAccumulatorState implements WorkObject{

	//TODO:not serialized for now (serialization is complex)
	private LongAccumulator accumulator;
	@JsonIgnore
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
