package org.rtm.commons;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Predicate;

import org.junit.Assert;
import org.junit.Test;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;

public class MeasurementUtilsTest {

	Map<String, Object> simpleMeasurement = TestMeasurementBuilder.buildStatic(TestMeasurementType.SIMPLE);
	Map<String, Object> optionalMeasurement = TestMeasurementBuilder.buildStatic(TestMeasurementType.WITH_OPTIONAL);

	@Test
	public void measurementURIConversionOracle(){
		
		System.out.println(simpleMeasurement);
		Assert.assertEquals(true, mapEquals(simpleMeasurement, MeasurementUtils.uriToMap(MeasurementUtils.mapToURI(simpleMeasurement))));
	}
	
	@Test
	public void testMapEquality(){
		Map<String, Object> m1 = new HashMap<>();
		Map<String, Object> m2 = new HashMap<>();
		m1.put("test", 1L);
		m1.put("test2", "s");
		m2.putAll(m1);
		
		Assert.assertEquals(true, mapEquals(m1, m2));
		
		m1 = new HashMap<>();
		m2 = new HashMap<>();
		
		Assert.assertEquals(true, mapEquals(m1, m2));
		Assert.assertEquals(true, mapEquals(null, null));
	}
	
	@Test
	public void testMapEqualityNeg(){
		Map<String, Object> m1 = new HashMap<>();
		Map<String, Object> m2 = new HashMap<>();
		m1.put("test", 1L);
		m1.put("test2", "s");
		
		Assert.assertEquals(false, mapEquals(m1, m2));
		Assert.assertEquals(false, mapEquals(m1, null));
	}

	public static boolean mapEquals(Map<String, Object> m1, Map<String, Object> m2){
		
		if((m1 == null && m2 == null) || (m1.size() == 0 && m2.size() == 0))
			return true;
		
		if(m1 == null || m2 == null || m1.size() == 0 || m2.size() == 0)
			return false;
		
		Predicate<Entry<String, Object>> p1 = e -> m2.get(e.getKey()) != null && equalsStringOrLong(m2.get(e.getKey()), e.getValue());
		Predicate<Entry<String, Object>> p2 = e -> m1.get(e.getKey()) != null && equalsStringOrLong(m1.get(e.getKey()), e.getValue());
		return m1.entrySet().stream().allMatch(p1) && m2.entrySet().stream().allMatch(p2);
	}
	
	public static boolean equalsStringOrLong(Object o1, Object o2){
		if(o1 instanceof String && o2 instanceof String)
			return ((String) o1).equals((String) o2);
		
		if(o1 instanceof Long && o2 instanceof Long)
			return ((Long) o1).equals((Long) o2);
		
		return false;
	}
	
}
