package org.rtm.metrics.accumulation.histograms;

import java.util.Iterator;

import com.google.common.collect.TreeMultimap;

public class Histogram {
	
	private int nbPairs;
	private int approxMs;
	private CountSumBucket[] histogram;
	
	public Histogram(int nbPairs, int approxMs){
		if(nbPairs > 0){
			this.nbPairs = nbPairs;
			this.histogram = new CountSumBucket[nbPairs];
		}else
			throw new RuntimeException("Illegal nbPairs: " + nbPairs);
		
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

	public synchronized void ingest(long valueMs){
		
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
		sb.append("(totalCount=");
		sb.append(getTotalCount());
		sb.append(", totalSum=");
		sb.append(getTotalSum());
		sb.append("), [");
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
	
	public long size(){
		return histogram.length;
	}
	
	private synchronized void mergeBucket(int index, CountSumBucket b){
		histogram[index].merge(b);
	}
	
	// public for JUnit test but should be private
	public CountSumBucket getBucket(int index){
		return histogram[index];
	}
	
	public synchronized void merge(Histogram hist) throws Exception{
		if(histogram.length != hist.size())
			throw new Exception("Inconsistent sizes: left=" + histogram.length +"; right="+ hist.size());
		
		Iterator<Integer> thisMap = buildSortedAvgMap().values().iterator();
		Iterator<Integer> thatMap = hist.buildSortedAvgMap().values().iterator();
		
		while(thisMap.hasNext() && thatMap.hasNext()){
			int leftIndex = thisMap.next();
			int rightIndex = thatMap.next();
			mergeBucket(leftIndex, hist.getBucket(rightIndex));
		}
		
		System.out.println("merged: " + this);
	}

	private TreeMultimap<Long, Integer> buildSortedAvgMap() {
		TreeMultimap<Long, Integer> map = TreeMultimap.create();
		for(int i=0; i<histogram.length; i++)
			map.put(histogram[i].getAvg(), i);
		return map;
	}

	public synchronized void reset() {
		this.histogram = new CountSumBucket[nbPairs];
		initArray();
	}
	
	public CountSumBucket[] getHistogramAsArray() {
		return histogram;
	}
	
	public Histogram diff(Histogram histogram2) throws Exception{
		
		if(this.nbPairs != histogram2.nbPairs)
			throw new Exception("Inconsistent number of pairs=" + this.nbPairs +" vs "+ histogram2.nbPairs);
		
		Histogram res = new Histogram(this.nbPairs, this.approxMs);
		CountSumBucket[] histogram2array = histogram2.getHistogramAsArray();
		CountSumBucket[] resArray = res.getHistogramAsArray();
		for(int i=0; i < this.nbPairs; i++){
			resArray[i] = this.histogram[i].diff(histogram2array[i]);
		}
		return res;
	}
	
	public long getValueForMark(float pcl) {
		long curCount = 0;
		long target = (long) (pcl * getTotalCount());
		for(int i=0; i<this.histogram.length; i++){
			curCount += histogram[i].getCount();
			if(curCount >= target){
				return histogram[i].getAvg();
			}
		}
		return -1;
	}
	
}
