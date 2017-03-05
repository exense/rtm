package org.rtm.pipeline.pull;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.pull.builders.PullPipelineBuilder;
import org.rtm.stream.result.ResultHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes","unused"})
public class PullPipeline{
	
	private static final Logger logger = LoggerFactory.getLogger(PullPipeline.class);

	private PullPipelineBuilder pb;
	private int poolSize;
	private ResultHandler rh;
	private BlockingMode mode;
	private long timeoutSecs;

	public PullPipeline(PullPipelineBuilder pb, int poolSize, long timeoutSecs, BlockingMode mode){
		this.pb = pb;
		this.poolSize = poolSize;
		this.timeoutSecs = timeoutSecs;
		this.mode = mode;
	}

	public void execute() throws Exception{

		ExecutorService singlePullPool = Executors.newFixedThreadPool(this.poolSize);
		IntStream.rangeClosed(1, poolSize).forEach( i -> singlePullPool.submit(pb.buildRunnable()));

		singlePullPool.shutdown();
		if(this.mode.equals(BlockingMode.BLOCKING))
			singlePullPool.awaitTermination(30, TimeUnit.SECONDS);
	}


}
