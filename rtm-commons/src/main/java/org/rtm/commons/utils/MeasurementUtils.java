package org.rtm.commons.utils;

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
import org.rtm.commons.RtmContext;

public class MeasurementUtils {

	private final RtmContext context;

	public MeasurementUtils(RtmContext context) {
		this.context = context;
	}

	public static Map<String, Object> stringToMap(String json) throws JsonParseException, JsonMappingException, IOException{
		ObjectMapper om = new ObjectMapper();
		Map<String, Object> map = new HashMap<String, Object>();

		map = om.readValue(json, new TypeReference<Map<String, Object>>(){});

		return map;
	}

	public String mapToURI(Map<String, Object> m){

		Map<String, Object> optional = new HashMap<>();
		optional.putAll(m);

		optional.remove(context.getEidKey());
		optional.remove(context.getBeginKey());
		optional.remove(context.getNameKey());
		optional.remove(context.getValueKey());

		StringBuilder sb = new StringBuilder();
		sb.append("/"); sb.append(m.get(context.getEidKey()));
		sb.append("/"); sb.append(m.get(context.getBeginKey()));
		sb.append("/"); sb.append(m.get(context.getNameKey()));
		sb.append("/"); sb.append(m.get(context.getValueKey()));
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

	public Map<String, Object> uriToMap(String uri){
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

	public Map<String, Object> structuredToMap(String eId, String time, String name, String value, String optionalKeyValuePairs) {
		Map<String, Object> map = new HashMap<>();
		map.put(context.getEidKey(), eId);
		map.put(context.getBeginKey(), Long.parseLong(time));
		map.put(context.getNameKey(), name);
		map.put(context.getValueKey(), Long.parseLong(value));

		if(optionalKeyValuePairs != null && !optionalKeyValuePairs.isEmpty())
			map.putAll(parseOptionalValues(optionalKeyValuePairs));

		return map;
	}

	public Map<String, Object> structuredToMap(String eId, String time, String name, String value) {
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
