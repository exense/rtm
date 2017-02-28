package org.rtm.range;

public abstract class OptimisticRangePartitioner<T extends Comparable<T>> {
	
	protected T min;
	protected T max;
	protected T incrementSize;
	
	protected OptimisticRangePartitioner(T min, T max, T incrementSize){
		this.min = min;
		this.max = max;
		this.incrementSize = incrementSize;
	}
	
	public abstract RangeBucket<T> next() throws IllegalStateException;
	public abstract boolean hasNext();

	
}
