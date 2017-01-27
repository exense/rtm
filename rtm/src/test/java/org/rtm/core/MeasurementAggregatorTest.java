package org.rtm.core;

import java.util.ArrayList;
import java.util.List;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class MeasurementAggregatorTest {

	List<Long> valueList;
	boolean init = false;

	@Before
	public void buildValueListAndInitMA(){
		if(!init){
			this.valueList = new ArrayList<Long>();
			for(long i=1; i<=200; i++)
				this.valueList.add(i);
		}
	}

	@Test
	public void testAverage(){
		Assert.assertEquals(100L, MeasurementAggregator.aggregateAverageByNumericVal(this.valueList));
	}

	@Test
	public void testMin(){
		Assert.assertEquals(1L, MeasurementAggregator.aggregateMinByNumericVal(this.valueList));
	}

	@Test
	public void testMax(){
		Assert.assertEquals(200L, MeasurementAggregator.aggregateMaxByNumericVal(this.valueList));
	}


	@Test
	public void testSum(){
		Assert.assertEquals(20100L, MeasurementAggregator.aggregateSumByNumericVal(this.valueList));
	}

	@Test
	public void testCount(){
		Assert.assertEquals(200L, MeasurementAggregator.aggregateCountByNumericVal(this.valueList));
	}


	@Test
	public void test99thPercentile(){
		Assert.assertEquals(198L, MeasurementAggregator.aggregatePercentileByNumericVal(this.valueList, 99D));
	}

	@Test
	public void testStandardDev(){
		Assert.assertEquals(57L, MeasurementAggregator.aggregateStandardDevByNumericVal(this.valueList));
	}

	@Test
	public void testStandardDevNoBias(){
		Assert.assertEquals(57L, MeasurementAggregator.aggregateStandardDevNoBiasByNumericVal(this.valueList));
	}


}
