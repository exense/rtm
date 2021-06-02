package org.rtm.commons;

import step.core.collections.Document;
import org.junit.Assert;
import org.junit.Test;


public class MeasurementConverterTest {

	@Test
	public void testConversion(){
		String json = "{ \"begin\" : \"14712342\", \"value\" : 223, \"name\" : \"MyTransaction\", \"clientIp\" : \"192.168.0.1\" }";
		Assert.assertEquals(true, (MeasurementDBConverter.convertToMongo(json) instanceof Document));
	}
	
	//@Test
	public void testConversionNeg(){
		String json = "{ \"begin\" : \"14712342\", \"value\" : 223, \"name\" : \"MyTransaction\", \"clientIp\" : 192.168.0.1 }";
		boolean exceptionRaised = false;
		try{
			MeasurementDBConverter.convertToMongo(json);
		} finally{
			Assert.assertEquals(true, exceptionRaised);
		}
	}
}
