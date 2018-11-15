package org.rtm.e2e.ingestion.load;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;

public class TransactionalProfile extends LoadDescriptor{

	private int skewFactor;
	private int stdFactor;
	private int targetCardinality;

	public TransactionalProfile(long pauseTime, int nbIterations, int nbTasks, int timeOut, int skewFactor, int stdFactor, int targetCardinality){
		super(pauseTime, nbIterations, nbTasks, timeOut);
		this.stdFactor = stdFactor;
		this.skewFactor = skewFactor;
		this.targetCardinality = targetCardinality;
	}

	@Override
	public Map<String, Object> getNextMeasurementForSend(int taskId) {
		
		int actualTaskId = (taskId % this.targetCardinality) + 1;
		
		return TestMeasurementBuilder.build(TestMeasurementType.SIMPLE,
				"TransactionalProfile",
				"Transaction_" + actualTaskId,
				new Date().getTime(),
				computeSkew(taskId),
				null);
	}

	private Long computeSkew(int taskId) {
		
		int actualTaskId = (taskId % this.targetCardinality) + 1;
		
		long targetDeviation = actualTaskId * stdFactor;
		long smallDeviation= ThreadLocalRandom.current().nextLong(0, targetDeviation > 0 ? targetDeviation : 1L);
		boolean plusMinus = ThreadLocalRandom.current().nextInt(0,2) == 1;
		Long value = new Long(actualTaskId * this.skewFactor);
		if(plusMinus)
			value += smallDeviation;
		else
			value -= smallDeviation;
		return value;
	}

}
