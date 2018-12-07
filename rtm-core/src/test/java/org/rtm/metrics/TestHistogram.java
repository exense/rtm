package org.rtm.metrics;

import java.util.TreeMap;
import java.util.concurrent.ThreadLocalRandom;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.histograms.CountSumBucket;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class TestHistogram {

	@Test
	public void testCountConsistency() throws Exception{

		long[] data = {5, 20, 32, 7, 12, 153, 28};

		Histogram myHisto = new Histogram(7, 10);
		long verifier = 0;
		for( long l : data){
			myHisto.ingest(l);
			verifier += l;
		}

		Assert.assertEquals(verifier, myHisto.getTotalSum());
	}

	@Test
	public void testMergeConsistency() throws Exception{

		long[] data1 = {5, 20};
		long[] data2 = {20, 5};

		/*	long[] data1 = {5, 20, 32, 7, 12, 153, 28};
			long[] data2 = {28, 20, 32, 7, 12, 153, 5};	*/

		Histogram myHisto1 = new Histogram(7, 10);
		Histogram myHisto2 = new Histogram(7, 10);

		for( long l : data1)
			myHisto1.ingest(l);

		for( long l : data2)
			myHisto2.ingest(l);

		myHisto1.merge(myHisto2);

		Assert.assertEquals(data1[1] + data2[0], myHisto1.getBucket(1).getSum());
	}

	@Test
	public void testBuildBucketMapByAverage(){
		Histogram h1 = new Histogram(10,10);
		Histogram h2 = new Histogram(10,10);
		h1.ingest(100L);
		h2.ingest(100L);

		try {
			h1.merge(h2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Assert.assertEquals(2, h1.buildBucketMapByAverage().get(100L).getCount());
	}

	@Test
	public void testMergeSameKey(){
		Histogram h1 = new Histogram(2,10);
		h1.ingest(100L);
		h1.ingest(110L);
		Assert.assertEquals(105, h1.getHistogram()[0].getAvg());
		h1.ingest(105L);
		Assert.assertEquals(105, h1.getHistogram()[0].getAvg());

		Assert.assertEquals(0, h1.getHistogram()[1].getCount());
		h1.ingest(116L);
		Assert.assertEquals(116, h1.getHistogram()[1].getAvg());
		System.out.println(h1);


		Histogram h2 = new Histogram(2,10);		
		h2.ingest(105L);
		h2.ingest(116L);

		System.out.println(h2);
		try {
			h1.merge(h2);
		} catch (Exception e) {
			e.printStackTrace();
		}

		TreeMap<Long, CountSumBucket> map = h1.buildBucketMapByAverage();
		System.out.println(map);
		Assert.assertEquals(2, map.size());
	}

	@Test
	public void testSelfMerge() throws Exception{
		Histogram h1 = new Histogram(3,1);
		h1.ingest(0L);
		h1.ingest(2L);
		h1.ingest(4L);

		h1.merge(h1);
		h1.merge(h1);
		Assert.assertEquals(24, h1.getTotalSum());
		Assert.assertEquals(12, h1.getTotalCount());
	}

	@Test
	public void testNoKeyCollision() throws Exception{
		
		Histogram h1 = new Histogram(3,1);
		h1.ingest(1);
		h1.ingest(3);
		h1.ingest(6);
		h1.ingest(6);
		h1.ingest(6);
		h1.ingest(5);
		
		Histogram h2 = new Histogram(3,1);
		h2.ingest(5);
		h2.ingest(3);
		h2.ingest(6);
		h2.ingest(7);
		h2.ingest(2);
		h2.ingest(3);
		
		h1.merge(h2);
		
		h1.buildBucketMapByAverage();
		
	}
	
	@Test
	public void testRandomCollisionGenerator() throws Exception{
		for(int k=0; k<100000; k++){
			//System.out.println("---------------------------------------------------------------------");
			Histogram h1 = new Histogram(3,1);
			for(int i = 0; i<6; i++){
				int rand = ThreadLocalRandom.current().nextInt(0, 8);
				//System.out.println("--h1-- "+ rand);
				h1.ingest(rand);
			}

			for(int i = 0; i<6; i++){
				Histogram h2 = new Histogram(3,1); 

				for(int j = 0; j<6; j++){
					int rand = ThreadLocalRandom.current().nextInt(0, 8);
					//System.out.println("--h2-- " + rand);
					h2.ingest(rand);
				}
				//System.out.println("[h1] " + h1);
				//System.out.println("[h2] " + h2);
				h1.merge(h2);
				//System.out.println("[mrged] " + h1);
				h1.buildBucketMapByAverage();
			}
		}
	}
}
