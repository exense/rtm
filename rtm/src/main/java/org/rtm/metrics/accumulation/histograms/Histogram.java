package org.rtm.metrics.accumulation.histograms;

import java.util.Iterator;

import org.rtm.db.DBClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.google.common.collect.TreeMultimap;

public class Histogram {
	private static final Logger logger = LoggerFactory.getLogger(DBClient.class);
	
	private int nbPairs;
	private int approxMs;
	
	//TODO: reimplement with Map<Integer, CountSumBucker> to find match & closest faster
	public CountSumBucket[] histogram;
	
	public Histogram(){
	}
	
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
	
	public void initArray() {
		this.histogram = new CountSumBucket[nbPairs];
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
			insert(findClosest(valueMs,minBound, maxBound), valueMs);
	}
	
	public synchronized void ingest(CountSumBucket toBeMerged){
		
		long csbAvg = toBeMerged.getAvg();
		long minBound = csbAvg - approxMs;
		long maxBound = csbAvg + approxMs;
		
		int bucketId = matchExisting(minBound, maxBound);
		
		if(bucketId > -1)
			insert(bucketId, toBeMerged);
		else
			insert(findClosest(csbAvg,minBound, maxBound), toBeMerged);
	}

	private int findClosest(long valueMs, long minBound, long maxBound) {
		int curIndex = -1;
		long minDiff = Long.MAX_VALUE;
		
		for(int i=0; i < histogram.length; i++){
			long avg = histogram[i].getAvg();
			if(avg < 0) {// empty bucket
				//System.out.println("new bucket: value=" + valueMs + ", minBound=" + minBound + ", maxBound=" + maxBound);
				return i;
			}
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

	private void insert(int bucketId, CountSumBucket toBeMerged) {
		histogram[bucketId].merge(toBeMerged);
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
	
	// 1 to 1 index merge: fast but not precise
	@Deprecated
	public synchronized void oldmerge(Histogram hist) throws Exception{
		
		if(histogram == null || hist == null)
			throw new Exception("Null histogram.");
		
		if(histogram.length != hist.size())
			throw new Exception("Inconsistent sizes: left=" + histogram.length +"; right="+ hist.size());
		
		Iterator<Integer> thisMap = buildSortedAvgMap().values().iterator();
		Iterator<Integer> thatMap = hist.buildSortedAvgMap().values().iterator();
		
		while(thisMap.hasNext() && thatMap.hasNext()){
			int leftIndex = thisMap.next();
			int rightIndex = thatMap.next();
			mergeBucket(leftIndex, hist.getBucket(rightIndex));
		}
	}
	
	public synchronized void merge(Histogram hist) throws Exception{
		CountSumBucket[] toBeMerged = hist.getHistogram();
		int size = toBeMerged.length;
		
		for(int i=0; i<size; i++)
			ingest(toBeMerged[i]);
	}

	private TreeMultimap<Long, CountSumBucket> buildBucketMapByAverage() {
		TreeMultimap<Long, CountSumBucket> map = TreeMultimap.create();
		for(int i=0; i<histogram.length; i++)
			map.put(histogram[i].getAvg(), histogram[i]);
		return map;
	}
	
	//Hack based on the TreeMultimap implementation
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
	
	public Histogram diff(Histogram histogram2) throws Exception{
		
		if(this.nbPairs != histogram2.nbPairs)
			throw new Exception("Inconsistent number of pairs=" + this.nbPairs +" vs "+ histogram2.nbPairs);
		
		Histogram res = new Histogram(this.nbPairs, this.approxMs);
		CountSumBucket[] histogram2array = histogram2.getHistogram();
		CountSumBucket[] resArray = res.getHistogram();
		for(int i=0; i < this.nbPairs; i++){
			resArray[i] = this.histogram[i].diff(histogram2array[i]);
		}
		return res;
	}
	
	public long getValueForMark(float pcl) {
		long curDotCount = 0;
		int bucketCount= 0;
		long target = (long) (pcl * getTotalCount());
		TreeMultimap<Long, CountSumBucket> sortedMap = buildBucketMapByAverage();
		Iterator<CountSumBucket> thisMap = sortedMap.values().iterator();
		
		while(thisMap.hasNext()){
			CountSumBucket curBucket = thisMap.next();
			curDotCount += curBucket.getCount();
			if(curDotCount >= target){
				int dotTarget = Math.round(pcl * getTotalCount());
				long missedTarget = curDotCount - dotTarget;
				float missedTargetRatio = ((float)missedTarget / (float)dotTarget);
				//int correctedValue = Math.round(curBucket.getAvg() / (1+missedTargetRatio));
				logger.debug("Ranked "+pcl+"th Pcl at bucket value " + curBucket.getAvg() + " with a dotCount of " + curDotCount + "/" + getTotalCount() + ", a bucketCount of " + bucketCount + "/" + sortedMap.size() + ", and a dot target of " +  dotTarget + ". Dot target was missed by " +  missedTarget + " dots (i.e " + (missedTargetRatio * 100) + "%)."
				//" + Using corrected value of: " + correctedValue
				);
				/* Bucket value */
				return curBucket.getAvg();
				/* Corrected value - this is not correct, the difference would have to be based on the diff between this bucket and the previous bucket*/
				//return correctedValue;
			}
			bucketCount++;
		}
		return -1;
	}

	public int getNbPairs() {
		return nbPairs;
	}

	public void setNbPairs(int nbPairs) {
		this.nbPairs = nbPairs;
	}

	public int getApproxMs() {
		return approxMs;
	}

	public void setApproxMs(int approxMs) {
		this.approxMs = approxMs;
	}

	public CountSumBucket[] getHistogram() {
		return histogram;
	}

	public void setHistogram(CountSumBucket[] histogram) {
		this.histogram = histogram;
	}
	
}
