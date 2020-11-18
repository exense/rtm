package org.rtm.metrics.accumulation.histograms;

import java.util.*;

import org.rtm.db.QueryClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Histogram {
	private static final Logger logger = LoggerFactory.getLogger(QueryClient.class);
	
	private int nbPairs;
	private int approxMs;

	private TreeMap<Long, CountSumBucket> histogram = new TreeMap();


	public Histogram(){
	}

	public Histogram(int nbPairs, int approxMs){
		if(nbPairs > 0){
			this.nbPairs = nbPairs;
		}else
			throw new RuntimeException("Illegal nbPairs: " + nbPairs);

		if(approxMs < 0)
			this.approxMs = approxMs * -1;
		else
			this.approxMs = approxMs;
	}

	private long getIndexOfValue(long value) {
		return (long) Math.floor(value / approxMs);
	}

	//TreeMap is not threadsafe, make sure write op are synchronized
	private synchronized CountSumBucket getBucketAt(long index) {
		if (histogram.containsKey(index)) {
			return histogram.get(index);
		} else if (histogram.size() < nbPairs) {
			CountSumBucket countSumBucket = new CountSumBucket();
			histogram.put(index,countSumBucket);
			return countSumBucket;
		} else {
			increaseApprox();
			return getBucketAt(index);
			//return findClosestBucket(index);
		}
	}

	//should only be called from getBucketAt which is synchronized
	private synchronized  void increaseApprox() {
		int newApprox = approxMs*2;
		Histogram newHistogram = new Histogram(nbPairs, newApprox);
		newHistogram.merge(this);
		this.histogram = newHistogram.getHistogram();
		this.approxMs = newApprox;

		//logger.debug("The maximum buckets for current histogram was reached; approximation was increased to " + this.approxMs);
	}

	//not giving the best results for increasing approx
	private long averageBucketGap(){
		Map.Entry<Long, CountSumBucket> firstEntry = histogram.firstEntry();
		Map.Entry<Long, CountSumBucket> lastEntry = histogram.lastEntry();
		return (lastEntry.getValue().getAvg() - firstEntry.getValue().getAvg())/ histogram.size();
	}

	private CountSumBucket findClosestBucket(long index) {
		if (histogram.containsKey(index)) {
			return histogram.get(index);
		} else {
			Long floorKey = histogram.floorKey(index);
			Long ceilingKey = histogram.ceilingKey(index);
			if (floorKey == null) {
				return histogram.get(ceilingKey);
			} else if (ceilingKey == null) {
				return histogram.get(floorKey);
			} else if (Math.abs(index - floorKey) < Math.abs(index - ceilingKey)) {
				return histogram.get(floorKey);
			} else {
				return histogram.get(ceilingKey);
			}
		}
	}

	public synchronized void ingest(long valueMs){
		CountSumBucket bucket = getBucketAt(getIndexOfValue(valueMs));
		bucket.ingest(valueMs);
	}

	public synchronized void ingest(CountSumBucket toBeMerged) {
		CountSumBucket bucket = getBucketAt(getIndexOfValue(toBeMerged.getAvg()));
		bucket.ingest(toBeMerged);
	}

	@Override
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append("(totalCount=");
		sb.append(getTotalCount());
		sb.append(", totalSum=");
		sb.append(getTotalSum());
		sb.append("), [");
		int i=0;
		for (CountSumBucket c : histogram.values()) {
			sb.append(c.toString());
			if(i < (histogram.size() - 1))
				sb.append(", ");
			i++;
		}
		sb.append("]");
		return sb.toString();
	}

	public long getVariance() {
		long mean = Math.round(getTotalSum() / getTotalCount());
		double variance = 0;
		for (CountSumBucket b: histogram.values()) {
			variance += (b.getAvg()-mean) * (b.getAvg()-mean);
		}
		variance = variance/histogram.size();
		return Math.round(variance);
	}

	public long getStdDeviation() {
		return Math.round( Math.sqrt(getVariance()));
	}

	public long getTotalCount(){
		long count = 0;
		for(CountSumBucket b : histogram.values())
			count += b.getCount();
		return count;
	}

	public long getTotalSum(){
		long sum = 0;
		for(CountSumBucket b : histogram.values())
			sum += b.getSum();
		return sum;
	}

	public long size(){
		return histogram.size();
	}

	private synchronized void mergeBucket(int index, CountSumBucket b){
		getBucketAt(index).ingest(b);
	}

	// public for JUnit test but should be private
	public CountSumBucket getBucket(long index){
		if (histogram.containsKey(index)) {
			return histogram.get(index);
		} else {
			return new CountSumBucket();
		}
	}

	public synchronized void merge(Histogram hist) {
		for (CountSumBucket b: hist.getHistogram().values()) {
			ingest(b);
		}
	}

	public Histogram diff(Histogram histogram2) throws Exception{

		if(this.nbPairs != histogram2.nbPairs)
			throw new Exception("Inconsistent number of pairs=" + this.nbPairs +" vs "+ histogram2.nbPairs);

		Histogram res = new Histogram(this.nbPairs, this.approxMs);
		Map<Long, CountSumBucket> resBuckets = res.getHistogram();
		for (Long index: histogram.keySet()) {
			resBuckets.put(index, this.histogram.get(index).diff(histogram2.getBucketAt(index)));
		}
		return res;
	}

	public long getValueForMark(float pcl) {
		long curDotCount = 0;
		int bucketCount= 0;
		long target = (long) Math.ceil(pcl * getTotalCount());

		Iterator<CountSumBucket> thisMap = histogram.values().iterator();

		while(thisMap.hasNext()){
			CountSumBucket curBucket = thisMap.next();
			curDotCount += curBucket.getCount();
			if(curDotCount >= target){
				int dotTarget = (int) Math.ceil(pcl * getTotalCount());
				long missedTarget = curDotCount - dotTarget;
				float missedTargetRatio = ((float)missedTarget / (float)dotTarget);
				if (logger.isDebugEnabled())
					logger.debug("Ranked "+pcl+"th Pcl at bucket value " + curBucket.getAvg() + " with a dotCount of " + curDotCount + "/" + getTotalCount() + ", a bucketCount of " + bucketCount + "/" + histogram.size() + ", and a dot target of " +  dotTarget + ". Dot target was missed by " +  missedTarget + " dots (i.e " + (missedTargetRatio * 100) + "%). Amount of used buckets=" + histogram.size() + "/" + this.nbPairs);

				return curBucket.getAvg();
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

	public TreeMap<Long,CountSumBucket> getHistogram() {
		return histogram;
	}


	
}
