package org.rtm.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

//TODO: needs to auto-pool
public class JSONMapper {
	
	private ObjectMapper or;
	
	public JSONMapper(){
		or = new ObjectMapper();
	}

	public <T> T convertObjectToType(Object o, Class<T> type) throws JsonProcessingException{
		return or.convertValue(o, type);
	}

	public String convertToJsonString(Object o) throws JsonProcessingException{
		return or.writeValueAsString(o);
	}

}
