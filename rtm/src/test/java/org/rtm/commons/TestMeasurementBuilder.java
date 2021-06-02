package org.rtm.commons;

import ch.exense.commons.app.Configuration;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
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
		Configuration configuration = null;
		try {
			configuration = new Configuration(new File("src/main/resources/rtm.properties"));
			RtmContext context = new RtmContext(configuration);
			
			Map<String, Object> map = new HashMap<>();
			map.put(context.getEidKey(), eId);
			map.put(context.getNameKey(), name);
			map.put(context.getBeginKey(), time);
			map.put(context.getValueKey(), value);
			
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
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		} catch (InvocationTargetException e) {
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			e.printStackTrace();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

}
