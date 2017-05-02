package org.rtm.metrics;

import org.junit.Test;
import org.rtm.metrics.accumulation.histograms.Histogram;

public class TestHistogram {
	
	@Test
	public void testCountConsistency() throws Exception{
		
		long[] data = {5, 20, 32, 7, 12, 153, 28};
		
		Histogram myHisto = new Histogram(7, 10);
		
		for( long l : data){
			myHisto.ingest(l);
		}
		
		System.out.println(myHisto);
		System.out.println(myHisto.getTotalCount());
		System.out.println(myHisto.getTotalSum());
	}

}
