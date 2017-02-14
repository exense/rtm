package org.rtm.commons;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;

public class TestMeasurementBuilder {
	
	public static enum TestMeasurementType {
		SIMPLE,
		WITH_OPTIONAL,
		SKEWED
	}
	
	public static Map<String, Object> buildStatic(TestMeasurementType type){

		Map<String, Object> optional = new HashMap<>(); 
		optional.put("this", "that");
		optional.put("foo", 956L);

		return build(type, "JUnit_Static", "Transaction", 1486936771L, 223L, optional);
	}

	public static Map<String, Object> buildDynamic(TestMeasurementType type){

		Map<String, Object> optional = new HashMap<>(); 
		optional.put(UUID.randomUUID().toString().split("-")[0],UUID.randomUUID().toString().split("-")[0]);
		optional.put(UUID.randomUUID().toString().split("-")[0], new Long(ThreadLocalRandom.current().nextInt(1, 5000)));

		return build(type, "JUnit_Dynamic", "Transaction_" + ThreadLocalRandom.current().nextInt(1, 10),
		new Date().getTime(), new Long(ThreadLocalRandom.current().nextInt(1, 5000)).longValue(), optional);

	}
	
	public static Map<String, Object> build(TestMeasurementType type, String eId, String name, Long time, Long value, Map<String, Object> optionals){
		Map<String, Object> map = new HashMap<>();
		map.put(MeasurementConstants.EID_KEY, eId);
		map.put(MeasurementConstants.NAME_KEY, name);
		map.put(MeasurementConstants.BEGIN_KEY, time);
		map.put(MeasurementConstants.VALUE_KEY, value);
		
		switch (type){

		case SIMPLE:
			// do nothing
			break;
		case WITH_OPTIONAL:
			map.putAll(optionals);
			break;
		default:
			//do nothing;
			break;
		}
		
		return map;
	}

}
