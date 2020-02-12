package org.rtm.stream;

import java.io.IOException;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.metrics.accumulation.base.CountAccumulator;
import org.rtm.metrics.accumulation.base.CountAccumulator.CountAccumulatorState;
import org.rtm.metrics.accumulation.base.HistogramAccumulatorState;
import org.rtm.metrics.accumulation.base.MaxAccumulator;
import org.rtm.metrics.accumulation.base.MaxAccumulatorState;
import org.rtm.metrics.accumulation.base.MinAccumulator;
import org.rtm.metrics.accumulation.base.MinAccumulatorState;
import org.rtm.metrics.accumulation.base.SumAccumulator;
import org.rtm.metrics.accumulation.base.SumAccumulatorState;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;


public class LongRangeValueSerializationTest {

	@Test
	public void test() throws JsonParseException, JsonMappingException, IOException {
		
		MaxAccumulatorState maxAccumulatorState = new MaxAccumulatorState();
		MaxAccumulator maxAccumulator = new MaxAccumulator();
		maxAccumulator.accumulate(maxAccumulatorState, 100l);
		
		MinAccumulatorState minAccumulatorState = new MinAccumulatorState();
		MinAccumulator minAccumulator = new MinAccumulator();
		minAccumulator.accumulate(minAccumulatorState, -100l);
		
		SumAccumulatorState sumAccumulatorState = new SumAccumulatorState();
		SumAccumulator sumAccumulator = new SumAccumulator();
		sumAccumulator.accumulate(sumAccumulatorState, 15l);
		
		CountAccumulatorState countAccumulatorState = new CountAccumulatorState();
		CountAccumulator countAccumulator = new CountAccumulator();
		countAccumulator.accumulate(countAccumulatorState, 13L);
		countAccumulator.accumulate(countAccumulatorState, 13L);
		
		LongRangeValue longRangeValue = new LongRangeValue(1l);
		WorkDimension dimension = new WorkDimension("test");
		dimension.put("myAccumulator", new HistogramAccumulatorState(11,12));
		dimension.put("myOtherAccumulator", countAccumulatorState);
		dimension.put("maxAccumulator", maxAccumulatorState);
		dimension.put("minAccumulator", minAccumulatorState);
		dimension.put("sumAccumulator", sumAccumulatorState);
		longRangeValue.setDimension(dimension);
		
		ObjectMapper m = new ObjectMapper();
		String jsonString = m.writeValueAsString(longRangeValue);
		
		LongRangeValue actual = m.readValue(jsonString, LongRangeValue.class);
		Assert.assertNotNull(actual.getDimension("test"));
		HistogramAccumulatorState myAccumulator = (HistogramAccumulatorState) actual.getDimension("test").get("myAccumulator");
		Assert.assertNotNull(myAccumulator);
		Assert.assertEquals(11, myAccumulator.getNbPairs());
		Assert.assertEquals(12, myAccumulator.getApproxMs());
		
		CountAccumulatorState myOtherAccumulator = (CountAccumulatorState)actual.getDimension("test").get("myOtherAccumulator");
		Assert.assertEquals(2, myOtherAccumulator.getAccumulatorValue());
		
		MaxAccumulatorState maxAccumulatorActual = (MaxAccumulatorState) actual.getDimension("test").get("maxAccumulator");
		Assert.assertEquals(100, maxAccumulatorActual.getAccumulator().get());
		
		MinAccumulatorState minAccumulatorActual = (MinAccumulatorState) actual.getDimension("test").get("minAccumulator");
		Assert.assertEquals(-100, minAccumulatorActual.getAccumulator().get());
		
		SumAccumulatorState sumAccumulatorActual = (SumAccumulatorState) actual.getDimension("test").get("sumAccumulator");
		Assert.assertEquals(15, sumAccumulatorActual.getAccumulator().get());
	}

}
