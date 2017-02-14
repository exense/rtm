package org.rtm.e2e.ingestion.load;

import java.util.Map;

import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;

public class SimpleLoadDescriptor extends LoadDescriptor {

	public SimpleLoadDescriptor(){
		super(100L, 10, 10);
	}

	@Override
	public Map<String, Object> getNextMeasurementForSend(int id) {
		return TestMeasurementBuilder.buildDynamic(TestMeasurementType.SIMPLE);
	}
}
