package org.rtm.range;

public abstract class RangePartitioner<T extends Comparable<T>> {
	
	protected T min;
	protected T max;
	protected T incrementSize;
	
	protected RangePartitioner(T min, T max, T incrementSize){
		this.min = min;
		this.max = max;
		this.incrementSize = incrementSize;
	}
	
	public abstract RangeBucket<T> next() throws IllegalStateException;
	public abstract boolean hasNext();

	
}
