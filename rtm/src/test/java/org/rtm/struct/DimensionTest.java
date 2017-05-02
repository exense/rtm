package org.rtm.struct;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.LongAccumulationHelper;
import org.rtm.stream.Dimension;

public class DimensionTest {

	@Test
	public void testAccumulation(){
		Dimension d = new Dimension("Transaction1", 10, 20);
		LongAccumulationHelper acc = d.getAccumulationHelper();
		acc.initializeAccumulatorForMetric("count", (x, y) -> x+1, 0L);
		acc.accumulateMetric("count", 1);
		acc.accumulateMetric("count", 1);
		acc.accumulateMetric("count", 1);
		
		d.copyAndFlush();
		 
		Assert.assertEquals(3L, (long)d.get("count"));
	}
}
