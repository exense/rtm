package org.rtm.metrics;

import org.junit.Assert;
import org.junit.Test;
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

		Assert.assertEquals(2, h1.buildBucketMapByAverage().get(100L).first().getCount());
	}

}
