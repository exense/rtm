package org.rtm.pipeline;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.pipeline.builders.pipeline.RunableBuilder;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.commons.Pipeline;
import org.rtm.stream.result.ResultHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes","unused"})
public class PullPipelineExecutor implements Pipeline{
	
	private static final Logger logger = LoggerFactory.getLogger(PullPipelineExecutor.class);

	private RunableBuilder pb;
	private int poolSize;
	private ResultHandler rh;
	private BlockingMode mode;
	private long timeoutSecs;

	public PullPipelineExecutor(RunableBuilder pb, int poolSize, long timeoutSecs, BlockingMode mode){
		this.pb = pb;
		this.poolSize = poolSize;
		this.timeoutSecs = timeoutSecs;
		this.mode = mode;
	}

	public void execute() throws Exception{

		ExecutorService singlePullPool = Executors.newFixedThreadPool(this.poolSize);
		
		//logger.debug("Starting Pool execution with " + this.poolSize + " threads.");
		IntStream.rangeClosed(1, poolSize).forEach( i -> singlePullPool.submit(
				pb.buildRunnable()
				));
		
		singlePullPool.shutdown();
		
		if(this.mode.equals(BlockingMode.BLOCKING)){
			long start = System.currentTimeMillis();
			singlePullPool.awaitTermination(this.timeoutSecs, TimeUnit.SECONDS);
			//logger.debug(Thread.currentThread() +" waited " + (System.currentTimeMillis() - start) + " ms.");
		}
	}


}
