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

import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.exception.ValidationException;

public class AggregateValidator extends InputValidator{

	public static final String serviceDomain = Configuration.getInstance().getProperty("aggregateService.domain");
	
	@Override
	public void validateCustom(ServiceInput input) throws ValidationException {		
		if(testVal(Measurement.SESSION_KEY))
			throw new ValidationException("No session value was found");
		
		if(testVal(serviceDomain + "." + Configuration.GROUPBY_KEY))
			throw new ValidationException("No groupby value was found");
		
		if(testVal(serviceDomain + "." + Configuration.GRANULARITY_KEY))
			throw new ValidationException("No granularity value was found");

		
	}
}
