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
