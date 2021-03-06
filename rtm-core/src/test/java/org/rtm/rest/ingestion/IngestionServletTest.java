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
package org.rtm.rest.ingestion;

import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.utils.MeasurementUtils;

public class IngestionServletTest {

	String eId = "JUnit";
	String name = "Transaction1";
	String time = "1486936771";
	String value = "223";
	String optional = "this=that;foo=956";
	String nullOptional = null;
	String emptyOptional = null;

	@Test
	public void buildSimpleMeasurement(){
		Map<String, Object> measurement = MeasurementUtils.structuredToMap(eId, time, name, value, null);
		Assert.assertEquals(4, measurement.size());
	}

	@Test
	public void buildMeasurementWithOptionalData(){
		Map<String, Object> measurement = MeasurementUtils.structuredToMap(eId, time, name, value, optional);
		boolean autoLongConversion = false;
		if(measurement.get("foo") instanceof Long)
			autoLongConversion = true;
		Assert.assertEquals(true, autoLongConversion);
		Assert.assertEquals(6, measurement.size());
	}
}