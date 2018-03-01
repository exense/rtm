package org.rtm.metrics;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.base.SumAccumulator;
import org.rtm.range.RangeBucket;

public class AccumulationManagerTest {

	@Test	
	public void testAccumulators() throws Exception {
		
		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));
		
		AccumulationManager metricsManager = new AccumulationManager(props);
		
		metricsManager.accumulate(new RangeBucket<Long>(0L, 1L), 75L);
		metricsManager.accumulate(new RangeBucket<Long>(1L, 2L), 380L);
		metricsManager.accumulate(new RangeBucket<Long>(2L, 3L), 110L);
		
		System.out.println(metricsManager.getAllValues());
		Assert.assertEquals(565L, metricsManager.getValueForAccumulator(SumAccumulator.class.getName()));
	}
}
