package org.rtm.time;

public class RangeBucket<T extends Comparable<T>> implements Identifier<T>{

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

	@Override
	public Identifier<T> getId() {
		return this;
	}

	@Override
	public T getIdAsTypedObject() {
		return this.lowerBound;
	}

	@Override
	public int compareTo(T o) {
		if(this.lowerBound.compareTo(o) < 0)
			return -1;
		if(this.lowerBound.compareTo(o) > 0)
			return 1;
		return 0;
	}

}
