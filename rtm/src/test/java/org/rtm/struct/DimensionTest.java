package org.rtm.struct;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAccumulator;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongAccumulationHelper;

public class DimensionTest {

	@SuppressWarnings("rawtypes")
	@Test
	public void quicky(){
		Dimension d = new Dimension("Transaction1");
		LongAccumulationHelper acc = d.getAccumulationHelper();
		acc.initializeAccumulatorForMetric("count", (x, y) -> x+1, 0L);
		//Map<String, Object> m = TestMeasurementBuilder.buildDynamic(TestMeasurementType.SIMPLE);
		acc.accumulateMetric("count", 1);
		acc.accumulateMetric("count", 1);
		acc.accumulateMetric("count", 1);
		
		Assert.assertEquals(null, d.get("Transaction1"));
		
		d.copyAndFlush();
		
		Assert.assertEquals(3L,((Map)d.get("Transaction1")).get("count"));
	}
}
