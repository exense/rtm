package org.rtm.buckets;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.IntStream;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.time.OptimisticLongPartitioner;
import org.rtm.time.RangeBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RangePartitionerTest{

	private static final Logger logger = LoggerFactory.getLogger(OptimisticLongPartitioner.class);

	@Test
	public void testSimpleIncrement(){
		long start = 0L;
		long end = 100L;
		long inc = 10L;

		OptimisticLongPartitioner lp = new OptimisticLongPartitioner(start, end, inc);

		int bucketNb = 0;
		int sum = 0;

		while(lp.hasNext()){
			RangeBucket<Long> b = lp.next();
			bucketNb++;
			sum += b.getUpperBound();
		}
		validateTestNoLimit("testSimpleIncrement", start, end, inc, bucketNb, sum);
	}

	@Test
	public void testSimpleIncrementWithLimit(){
		long start = 0L;
		long end = 100L;
		long inc = 9L;

		OptimisticLongPartitioner lp = new OptimisticLongPartitioner(start, end, inc);

		int bucketNb = 0;
		int sum = 0;
		while(lp.hasNext()){
			RangeBucket<Long> b = lp.next();
			bucketNb++;
			sum += b.getUpperBound();
		}
		validateTestManually("testSimpleIncrementWithLimit", start, end, inc, bucketNb, sum, 12, 694);
	}

	@Test
	public void testLongPartitionerParallelSmallRange() throws InterruptedException, ExecutionException{

		int parallelism = 1; long start = 0L; long end = 100L;	long inc = 9L; int timeout = 5;

		Map<String, Integer> res = parallelTester(parallelism, start, end, inc, timeout);
		validateTestManually("testLongPartitionerParallelSmallRange", start, end, inc, res.get("count"), res.get("sum"), 12, 694);
	}
	
	@Test
	public void testLongPartitionerParallelLargeRange() throws InterruptedException, ExecutionException{
		int parallelism = 2; long start = 0L; long end = 1000L;	long inc = 9L; int timeout = 5;

		Map<String, Integer> res = parallelTester(parallelism, start, end, inc, timeout);
		validateTestManually("testLongPartitionerParallelLargeRange", start, end, inc, res.get("count"), res.get("sum"), 112, 56944);

	}

	public Map<String, Integer> parallelTester(int parallelism, long start,	long end, long inc, int timeout) throws InterruptedException, ExecutionException{
		OptimisticLongPartitioner lp = new OptimisticLongPartitioner(start, end, inc);

		int bucketNb = 0;
		int sum = 0;
		Vector<Callable<List<RangeBucket<Long>>>> tasks = new Vector<>();
		ExecutorService executor = Executors.newFixedThreadPool(parallelism);
		IntStream.rangeClosed(1, parallelism).forEach(
				i -> tasks.addElement(
						() -> {
							List<RangeBucket<Long>> l = new ArrayList<>();
							while(lp.hasNext()){
								l.add(lp.next());
							}
							return l;
						}
						));

		for(Future<List<RangeBucket<Long>>> f : executor.invokeAll(tasks, timeout, TimeUnit.SECONDS)){
			//logger.debug(f.toString());
			for(RangeBucket<Long> rb : f.get()){
				if(rb != null){
					sum+=rb.getUpperBound();
					bucketNb++;
					//logger.debug(rb.toString());
				}
			}
		}

		Map<String, Integer> result = new HashMap<>();
		result.put("sum", sum);
		result.put("count", bucketNb);
		return result;
	}

	private static boolean validateTestNoLimit(String testName, long start, long end, long inc, int effectiveBucketNb, int effectiveSum) {
		long targetNb = Math.abs((end-start) / inc);
		long targetUpperSum = 0;
		for(int i = 1; i <= targetNb; i++){
			targetUpperSum+= (i * inc);
		}

		logger.debug("ComputedValidation["+testName+"]: start="+start+"; end="+end+"; inc="+inc+"; bucketsFound="+effectiveBucketNb+"; targetBuckets="+targetNb+"; sumFound="+effectiveSum+"; targetSum="+targetUpperSum+";");
		Assert.assertEquals(targetNb, effectiveBucketNb);
		Assert.assertEquals(targetUpperSum, effectiveSum);

		if((effectiveBucketNb == targetNb) &&(effectiveSum == targetUpperSum))
			return true;
		return false;
	}

	private static boolean validateTestManually(String testName, long start, long end, long inc, int effectiveBucketNb, int effectiveSum, int targetNb, int targetUpperSum) {
		logger.debug("ManualValidation["+testName+"]: start="+start+"; end="+end+"; inc="+inc+"; bucketsFound="+effectiveBucketNb+"; targetBuckets="+targetNb+"; sumFound="+effectiveSum+"; targetSum="+targetUpperSum+";");
		Assert.assertEquals(targetNb, effectiveBucketNb);
		Assert.assertEquals(targetUpperSum, effectiveSum);

		if((effectiveBucketNb == targetNb) &&(effectiveSum == targetUpperSum))
			return true;
		return false;
	}

	@Test
	public void longLoopTest()throws InterruptedException, ExecutionException{
		
		int duration = 30; // SECONDS
		
		ExecutorService executor = Executors.newFixedThreadPool(1);
		Future<?> f = executor.submit(()->{try {
			loopedTest();
		} catch (Exception e) {
			executor.shutdownNow();
		}});
		
		boolean timeoutReached = false;
		try {
			f.get(duration, TimeUnit.SECONDS);
		} catch (TimeoutException e) {
			// Actually expected behavior
			timeoutReached = true;
		}
		
		Assert.assertEquals(true, timeoutReached);
	}
	
	public void loopedTest() throws InterruptedException, ExecutionException{

		int parallelism = 1; long start = 0L; long end = 10000L; long inc = 9L;  int timeout = 5;

		Map<String, Integer> res = null;
		do{
			res = parallelTester(parallelism, start, end, inc, timeout);
			System.out.println(res);
		}
		while(validateTestManually("loopedTest", start, end, inc, res.get("count"), res.get("sum"), 1112, 5569444));

	}
}
