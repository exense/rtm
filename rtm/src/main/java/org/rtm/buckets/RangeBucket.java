package org.rtm.buckets;

public class RangeBucket<T> {

	private T lowerBound;
	private T upperBound;
	
	public RangeBucket(T lowerBound, T upperBound){
		this.setLowerBound(lowerBound);
		this.setUpperBound(upperBound);
	}

	public T getLowerBound() {
		return lowerBound;
	}

	public void setLowerBound(T lowerBound) {
		this.lowerBound = lowerBound;
	}

	public T getUpperBound() {
		return upperBound;
	}

	public void setUpperBound(T upperBound) {
		this.upperBound = upperBound;
	}
	
	public String toString(){
		return "{ \"lowerBound\" : "+this.lowerBound+ ", \"upperBound\" : "+this.upperBound + "}"; 
	}
}
