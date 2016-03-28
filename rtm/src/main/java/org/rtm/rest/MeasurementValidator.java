package org.rtm.rest;

import org.rtm.commons.Configuration;

public class MeasurementValidator extends InputValidator{

	public static final String serviceDomain = Configuration.getInstance().getProperty("measurementService.domain");
	
	@Override
	public void validateCustom(ServiceInput input) {
		

	}
}
