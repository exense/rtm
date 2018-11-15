package org.rtm.metrics;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.base.SumAccumulator;
import org.rtm.stream.WorkDimension;

public class AccumulationManagerTest {

	@Test	
	public void testAccumulators() throws Exception {
		
		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));
		
		AccumulationManager metricsManager = new AccumulationManager(props);
		
		WorkDimension dimension = new WorkDimension("test");
		
		metricsManager.accumulateAll(dimension, 75L);
		metricsManager.accumulateAll(dimension, 380L);
		metricsManager.accumulateAll(dimension, 110L);
		
		System.out.println(metricsManager.getAllValues(dimension));
		Assert.assertEquals(565L, metricsManager.getValueForAccumulator(dimension, SumAccumulator.class.getName()));
	}
}
