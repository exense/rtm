package org.rtm.metrics.accumulation.histograms;

public class CountSumBucket {

	private long count;
	private long sum;
	
	public CountSumBucket() {
		this.count = 0;
		this.sum = 0;
	}
	
	public synchronized void ingest(long value){
		this.count++;
		this.sum += value;
	}
	
	public long getCount() {
		return count;
	}
	
	public long getSum() {
		return sum;
	}

	public long getAvg() {
		if(count > 0)
			return this.sum / this.count;
		else
			return -1;
	}

	@Override
	public String toString(){
		return "{ avg="+getAvg()+", count="+count+", sum="+sum+"}";
	}
	
	public synchronized void merge(CountSumBucket other) {
		this.count += other.getCount();
		this.sum += other.getSum();
	}
}
