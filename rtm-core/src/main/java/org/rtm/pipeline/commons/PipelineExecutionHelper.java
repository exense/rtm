package org.rtm.pipeline.commons;

import java.util.concurrent.Executors;

import org.rtm.stream.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PipelineExecutionHelper {


	private static final Logger logger = LoggerFactory.getLogger(PipelineExecutionHelper.class);
	
	public static void executeAndsetListeners(Pipeline pp, Stream<Long> stream) {
		Runnable waiter = new Runnable() {

			@Override
			public void run() {
				try {
					pp.execute();
				} catch (Exception e) {
					e.printStackTrace();
				}finally{
					logger.info("Stream with id=" + stream.getId() + " completed with size=" + stream.getStreamData().size() + " and duration " + stream.getDurationMs() + " (started at " + stream.getTimeCreated() + ").");
					stream.setComplete(true);
				}
			}
		};

		Executors.newSingleThreadExecutor().submit(waiter);
	}

}
