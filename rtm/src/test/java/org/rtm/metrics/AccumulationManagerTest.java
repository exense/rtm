package org.rtm.metrics;

import java.io.File;
import java.io.FileReader;
import java.util.Properties;

import ch.exense.commons.app.Configuration;
import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.base.HistogramAccumulator;
import org.rtm.metrics.accumulation.base.HistogramAccumulator.HistogramAccumulatorState;
import org.rtm.stream.WorkDimension;

public class AccumulationManagerTest {

	@Test	
	public void testAccumulators() throws Exception {
		
		Properties props = new Properties();
		props.load(new FileReader(new File("src/main/resources/rtm.properties")));
		Configuration configuration = new Configuration(new File("src/main/resources/rtm.properties"));
		
		AccumulationManager metricsManager = new AccumulationManager(props, configuration);
		
		WorkDimension dimension = new WorkDimension("test");
		
		metricsManager.accumulateAll(dimension, 75L);
		metricsManager.accumulateAll(dimension, 380L);
		metricsManager.accumulateAll(dimension, 110L);
		
		System.out.println(metricsManager.getAllValues(dimension));
		Assert.assertEquals(565L, ((HistogramAccumulatorState)metricsManager.getValueForAccumulator(dimension, HistogramAccumulator.class.getName())).getTotalSum());
	}
}
