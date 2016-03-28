package org.rtm.rest;

import java.util.List;
import java.util.Map;

import org.rtm.commons.Configuration;
import org.rtm.dao.Selector;
import org.rtm.exception.ValidationException;

public abstract class InputValidator {
	
	public void validate(ServiceInput input) throws ValidationException{
		List<Selector> ls = input.getSelectors();
		Map<String,String> serviceParams = input.getServiceParams();
		
		if(testList(ls))
			throw new ValidationException("No selector was found");
		
		validateCustom(input);
	}

	public abstract void validateCustom(ServiceInput input) throws ValidationException;
	
	public static boolean testVal(String s){
		return (s == null) || (s.trim().isEmpty());
	}
	
	@SuppressWarnings("rawtypes")
	public static boolean testList(List l){
		return (l == null || l.size() < 1);
	}
}
