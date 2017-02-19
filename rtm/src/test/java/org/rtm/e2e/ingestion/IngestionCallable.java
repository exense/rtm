package org.rtm.e2e.ingestion;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

import org.rtm.commons.TransportClient;
import org.rtm.commons.TransportException;
import org.rtm.e2e.ingestion.load.LoadDescriptor;
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

		boolean error = false;

		for(int i = 1; i <= ld.getNbIterations(); i++){
			Map<String, Object> m = ld.getNextMeasurementForSend(taskId);
			try{
				tc.sendStructuredMeasurement(m);
			} catch (TransportException e) {
				logger.error("Send failed for taskId: " + taskId + ", and measurement: " + m, e);
				error = true;
			}
			TimeUnit.MILLISECONDS.sleep(ld.getPauseTime());

		}
		return !error;
	}

}
