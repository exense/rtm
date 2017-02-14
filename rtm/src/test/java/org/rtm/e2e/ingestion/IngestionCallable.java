package org.rtm.e2e.ingestion;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.commons.ExceptionHandling;
import org.rtm.e2e.ingestion.load.LoadDescriptor;
import org.rtm.e2e.ingestion.transport.TransportClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IngestionCallable implements Callable<Boolean>{

	private static final Logger logger = LoggerFactory.getLogger(IngestionCallable.class);

	private TransportClient tc;
	private LoadDescriptor ld;
	private int taskId;

	public IngestionCallable(TransportClient tc, LoadDescriptor ld, int taskId){
		this.tc = tc;
		this.ld = ld;
		this.taskId = taskId;
	}

	@Override
	public Boolean call() throws Exception {

		List<String> errorList = new ArrayList<>();

		IntStream.rangeClosed(1, ld.getNbIterations()).forEach(
					nbr -> {
						try {
							tc.sendStructuredMeasurement(ld.getNextMeasurementForSend(taskId));
							TimeUnit.MILLISECONDS.sleep(ld.getPauseTime());
						} catch (Exception e) {
							errorList.add(ExceptionHandling.processExceptionAndReturnMessage(logger, e));
						}
					}
					);

		if(errorList.size() > 0)
			return false;
		else
			return true;
	}

}
