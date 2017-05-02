package org.rtm.metrics.accumulation.histograms;

public class Histogram {
	
	private int nbPairs;
	private int approxMs;
	private CountSumBucket[] histogram;
	
	public Histogram(int nbPairs, int approxMs) throws Exception {
		if(nbPairs > 0){
			this.nbPairs = nbPairs;
			this.histogram = new CountSumBucket[nbPairs];
		}else
			throw new Exception("Illegal nbPairs: " + nbPairs);
		
		if(approxMs < 0)
			this.approxMs = approxMs * -1;
		else
			this.approxMs = approxMs;
		
		initArray();
	}
	
	private void initArray() {
		for(int i=0; i < this.nbPairs; i++)
			histogram[i] = new CountSumBucket();
	}

	public void ingest(long valueMs){
		
		long minBound = valueMs - approxMs;
		long maxBound = valueMs + approxMs;
		
		int bucketId = matchExisting(minBound, maxBound);
		
		if(bucketId > -1)
			insert(bucketId, valueMs);
		else
			insert(findClosest(valueMs), valueMs);
	}

	private int findClosest(long valueMs) {
		int curIndex = -1;
		long minDiff = Long.MAX_VALUE;
		
		for(int i=0; i < histogram.length; i++){
			long avg = histogram[i].getAvg();
			if(avg < 0) // empty bucket
				return i;
			long diff = avg - valueMs;
			if(diff < minDiff){
				minDiff = diff;
				curIndex = i;
			}
		}
		return curIndex;
	}

	private void insert(int bucketId, long valueMs) {
		histogram[bucketId].ingest(valueMs);
	}

	private int matchExisting(long min, long max){
		for(int i=0; i < this.histogram.length; i++){
			long avg = this.histogram[i].getAvg(); 
			if(avg > min && avg <= max){
				return i;
			}
		}
		return -1;
	}
	
	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("[");
		int size = histogram.length;
		for(int i=0; i < size; i++){
			sb.append(histogram[i].toString());
			if(i < (size - 1))
				sb.append(", ");
		}
		sb.append("]");
		return sb.toString();
	}
	
	public long getTotalCount(){
		long count = 0;
		for(CountSumBucket b : histogram)
			count += b.getCount();
		return count;
	}
	
	public long getTotalSum(){
		long sum = 0;
		for(CountSumBucket b : histogram)
			sum += b.getSum();
		return sum;
	}
}
