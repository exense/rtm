package org.rtm.e2e.ingestion.load;

import java.util.Date;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

import org.rtm.commons.TestMeasurementBuilder;
import org.rtm.commons.TestMeasurementBuilder.TestMeasurementType;

public class TransactionalProfile extends LoadDescriptor{

	private int skewFactor;
	private int stdFactor;

	public TransactionalProfile(long pauseTime, int nbIterations, int nbTasks, int skewFactor, int stdFactor){
		super(pauseTime, nbIterations, nbTasks);
		this.stdFactor = stdFactor;
		this.skewFactor = skewFactor;
	}

	@Override
	public Map<String, Object> getNextMeasurementForSend(int taskId) {
		return TestMeasurementBuilder.build(TestMeasurementType.SIMPLE,
				"TransactionalProfile",
				"Transaction_" + taskId,
				new Date().getTime(),
				computeSkew(taskId),
				null);
	}

	private Long computeSkew(int taskId) {
		long smallDeviation= ThreadLocalRandom.current().nextLong(0, taskId * stdFactor);
		boolean plusMinus = ThreadLocalRandom.current().nextInt(0,1) == 1;
		Long value = new Long(taskId * this.skewFactor);
		if(plusMinus)
			value += smallDeviation;
		else
			value -= smallDeviation;
		return value;
	}

}
