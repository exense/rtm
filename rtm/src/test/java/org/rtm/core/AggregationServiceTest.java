/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.core;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.commons.MeasurementConstants;

@SuppressWarnings("rawtypes")
public class AggregationServiceTest {

	@Test
	public void testAggregates1PerBucket() throws Exception{
		
		String seriesName = "JUnit";
		String sessionName = "junit";

		long gran = 30000L;
		List<Map> lm = new ArrayList<>();
		AggregationService as = new AggregationService();

		long begin = 1485548400L;
		for(int i=0; i<100; i++){
			//debug, remove afterwards
			Map<String, Object> m = new HashMap<>();
			m.put(MeasurementConstants.NAME_KEY, seriesName);
			m.put(MeasurementConstants.BEGIN_KEY, begin);
			m.put(MeasurementConstants.VALUE_KEY, 1L);
			lm.add(m);
			begin += gran;
		}
		ComplexServiceResponse inconsistent = as.buildAggregatesForTimeInconsistent(
				sessionName, lm, gran, MeasurementConstants.NAME_KEY,
				MeasurementConstants.BEGIN_KEY, MeasurementConstants.END_KEY, MeasurementConstants.VALUE_KEY, MeasurementConstants.SESSION_KEY);
		//System.out.println(inconsistent.getPayload());

		ComplexServiceResponse consistent =
				AggregationService.makeDataConsistent(
						inconsistent,
						MeasurementConstants.SESSION_KEY, MeasurementConstants.BEGIN_KEY, MeasurementConstants.END_KEY, MeasurementConstants.NAME_KEY);
		//System.out.println(consistent.getPayload());
		Assert.assertEquals(consistent.getPayload().get(seriesName).size(), 100);
	}

	
	@Test
	public void testAggregatesAllInOneBucket() throws Exception{
		
		String seriesName = "JUnit";
		String sessionName = "junit";

		long gran = 30000L;
		List<Map> lm = new ArrayList<>();
		AggregationService as = new AggregationService();

		long begin = 1485548400L;
		for(int i=0; i<100; i++){
			//debug, remove afterwards
			Map<String, Object> m = new HashMap<>();
			m.put(MeasurementConstants.NAME_KEY, seriesName);
			m.put(MeasurementConstants.BEGIN_KEY, begin);
			m.put(MeasurementConstants.VALUE_KEY, 1L);
			lm.add(m);
			//begin += gran;
		}
		ComplexServiceResponse inconsistent = as.buildAggregatesForTimeInconsistent(
				sessionName, lm, gran, MeasurementConstants.NAME_KEY,
				MeasurementConstants.BEGIN_KEY, MeasurementConstants.END_KEY, MeasurementConstants.VALUE_KEY, MeasurementConstants.SESSION_KEY);
		//System.out.println(inconsistent.getPayload());

		ComplexServiceResponse consistent =
				AggregationService.makeDataConsistent(
						inconsistent,
						MeasurementConstants.SESSION_KEY, MeasurementConstants.BEGIN_KEY, MeasurementConstants.END_KEY, MeasurementConstants.NAME_KEY);
		//System.out.println(consistent.getPayload());
		Assert.assertEquals(consistent.getPayload().get(seriesName).size(), 1);
		Assert.assertEquals((Long) consistent.getPayload().get(seriesName).get(0).get("cnt"), new Long(100L));
	}

	
}
