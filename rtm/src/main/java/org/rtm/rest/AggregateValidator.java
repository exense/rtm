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
