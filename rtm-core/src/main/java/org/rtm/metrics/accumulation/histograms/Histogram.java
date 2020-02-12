package org.rtm.metrics.accumulation.histograms;

import java.util.Iterator;
import java.util.TreeMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Histogram {

	private static final Logger logger = LoggerFactory.getLogger(Histogram.class);

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

		//logger.debug("-----");
		//logger.debug("hist state at start = " + this);

		if(bucketId > -1){
			//logger.debug("inserting CSB " + toBeMerged + " into existing index " + bucketId + " (with avg="+histogram[bucketId].getAvg()+ ")");
			insert(bucketId, toBeMerged);
			//logger.debug("new CSB state = " + histogram[bucketId]);
		}
		else{
			int newIndex = findClosest(csbAvg,minBound, maxBound);
			//logger.debug("inserting CSB " + toBeMerged + " into new/free bucket with index " + newIndex);
			insert(newIndex, toBeMerged);
			//logger.debug("new CSB state = " + histogram[newIndex]);
		}
		//logger.debug("hist state at end = " + this);
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
		//too costly
		//mergePotentialTwins(bucketId);
	}

	private void insert(int bucketId, CountSumBucket toBeIngested) {
		histogram[bucketId].ingest(toBeIngested);
		// we have a new avg value for this bucket and we want to prevent same-avg buckets from that point on
		mergePotentialTwins(bucketId);
	}

	private int matchExisting(long min, long max){
		for(int i=0; i < this.histogram.length; i++){
			long avg = this.histogram[i].getAvg(); 
			if(avg >= min && avg <= max){
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

	@JsonIgnore
	public long getTotalCount(){
		long count = 0;
		for(CountSumBucket b : histogram)
			count += b.getCount();
		return count;
	}

	@JsonIgnore
	public long getTotalSum(){
		long sum = 0;
		for(CountSumBucket b : histogram)
			sum += b.getSum();
		return sum;
	}

	@JsonIgnore
	public long size(){
		return histogram.length;
	}

	private synchronized void mergeBucket(int index, CountSumBucket b){
		histogram[index].ingest(b);
	}

	// public for JUnit test but should be private
	public CountSumBucket getBucket(int index){
		return histogram[index];
	}

	public synchronized void merge(Histogram hist) throws Exception{
		CountSumBucket[] toBeMerged = hist.getHistogram();
		int size = toBeMerged.length;

		for(int i=0; i<size; i++){
			//merge only useful buckets
			if(toBeMerged[i].getCount() > 0)
				ingest(toBeMerged[i]);
		}
	}

	public TreeMap<Long, CountSumBucket> buildBucketMapByAverage() {
		TreeMap<Long, CountSumBucket> map = new TreeMap<>();
		for(int i=0; i<histogram.length; i++){
			long curAvg = histogram[i].getAvg();
			if(histogram[i].getCount() > 0){ // merge only used / useful histograms
				// Forcing the absence of collisions here means enforcing at ingest time which is too costly
				// We'll lose some buckets along the way (and so, some accuracy) but that way we'll be faster.
				// (typical accuracy vs speed tradeoff in our approach)
				mergePotentialTwins(i);
				CountSumBucket previous = map.put(curAvg, histogram[i]);
				if(previous != null){
					throw new RuntimeException("We had a collision on histogram key! Key=" + curAvg);
				}
			}
		}

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
		TreeMap<Long, CountSumBucket> sortedMap = buildBucketMapByAverage();
		Iterator<CountSumBucket> thisMap = sortedMap.values().iterator();

		while(thisMap.hasNext()){
			CountSumBucket curBucket = thisMap.next();
			curDotCount += curBucket.getCount();
			if(curDotCount >= target){
				int dotTarget = Math.round(pcl * getTotalCount());
				long missedTarget = curDotCount - dotTarget;
				float missedTargetRatio = ((float)missedTarget / (float)dotTarget);
				//int correctedValue = Math.round(curBucket.getAvg() / (1+missedTargetRatio));
				logger.debug("Ranked "+pcl+"th Pcl at bucket value " + curBucket.getAvg() + " with a dotCount of " + curDotCount + "/" + getTotalCount() + ", a bucketCount of " + bucketCount + "/" + sortedMap.size() + ", and a dot target of " +  dotTarget + ". Dot target was missed by " +  missedTarget + " dots (i.e " + (missedTargetRatio * 100) + "%). Amount of used buckets=" + sortedMap.size() + "/" + this.nbPairs
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

	private void mergePotentialTwins(int bucketId) {

		long avg = histogram[bucketId].getAvg();

		for(int i=0; i<histogram.length; i++){
			if(i==bucketId)
				continue;
			else{
				if(histogram[i].getAvg() == avg){
					histogram[bucketId].ingest(histogram[i]);
					histogram[i]=new CountSumBucket();
					mergePotentialTwins(bucketId);
				}
			}
		}

	}

}
