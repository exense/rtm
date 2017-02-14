package org.rtm.commons;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class MeasurementUtils {

	public static Map<String, Object> stringToMap(String json) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper om = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();

		map = om.readValue(json, new TypeReference<Map<String, Object>>(){});

		return map;
	}

	public static String mapToURI(Map<String, Object> m){

		Map<String, Object> optional = new HashMap<>();
		optional.putAll(m);

		optional.remove(MeasurementConstants.EID_KEY);
		optional.remove(MeasurementConstants.BEGIN_KEY);
		optional.remove(MeasurementConstants.NAME_KEY);
		optional.remove(MeasurementConstants.VALUE_KEY);

		StringBuilder sb = new StringBuilder();
		sb.append("/"); sb.append(m.get(MeasurementConstants.EID_KEY));
		sb.append("/"); sb.append(m.get(MeasurementConstants.BEGIN_KEY));
		sb.append("/"); sb.append(m.get(MeasurementConstants.NAME_KEY));
		sb.append("/"); sb.append(m.get(MeasurementConstants.VALUE_KEY));
		sb.append("/");

		optional.entrySet().stream().forEach(e -> 
		{
			sb.append(e.getKey());
			sb.append("=");
			sb.append(e.getValue());
			sb.append(";");
		});

		if(optional.size() == 0)
			sb.deleteCharAt(sb.length()-1);

		return sb.toString();
	}

	public static Map<String, Object> uriToMap(String uri){
		String[] uriTokens = trimArray(uri.split("/"));

		String optional = null;
		if(uriTokens.length > 4)
			optional = uriTokens[4];

		return structuredToMap(uriTokens[0], uriTokens[1], uriTokens[2], uriTokens[3], optional);
	}

	public static String[] trimArray(String[] array){
		Object[] trimmedArray = Arrays.asList(array).stream().filter(t -> isMeasurementValueLegal(t)).toArray();

		return Arrays.copyOf(trimmedArray, trimmedArray.length, String[].class);
	}

	public static boolean isMeasurementValueLegal(Object value){
		if(value == null)
			return false;

		if(value instanceof String)
			return !((String) value).isEmpty();

		return false;
	}

	public static Map<String, Object> structuredToMap(String eId, String time, String name, String value, String optionalKeyValuePairs) {
		Map<String, Object> map = new HashMap<>();
		map.put(MeasurementConstants.EID_KEY, eId);
		map.put(MeasurementConstants.BEGIN_KEY, Long.parseLong(time));
		map.put(MeasurementConstants.NAME_KEY, name);
		map.put(MeasurementConstants.VALUE_KEY, Long.parseLong(value));

		if(optionalKeyValuePairs != null && !optionalKeyValuePairs.isEmpty())
			map.putAll(parseOptionalValues(optionalKeyValuePairs));

		return map;
	}

	public static Map<String, Object> structuredToMap(String eId, String time, String name, String value) {
		return structuredToMap(eId, time, name, value, null);
	}

	public static Map<String, Object> parseOptionalValues(String optionalKeyValuePairs) {
		Map<String, Object> map = new HashMap<>();

		Matcher m = Pattern.compile("(.+?)=(.+?)(;|$)").matcher(optionalKeyValuePairs);

		while(m.find()){
			String s = m.group(2);
			if(StringUtils.isNumeric(s))
				map.put(m.group(1), Long.parseLong(s));
			else
				map.put(m.group(1), s);
		}
		return map;
	}
}
