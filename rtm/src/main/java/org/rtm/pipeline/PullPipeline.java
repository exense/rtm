package org.rtm.pipeline;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.pipeline.builders.push.PushCallableBuilder;
import org.rtm.pipeline.callables.push.split.SplitCallable;
import org.rtm.stream.result.ResultHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes"})
public class PullPipeline{

	public enum BlockingMode{
		BLOCKING, NON_BLOCKING;
	}
	
	//private static final Logger logger = LoggerFactory.getLogger(SplitExecHarvestPipeline.class);

	private PushCallableBuilder cb;
	private int poolSize;
	private ResultHandler rh;
	private BlockingMode mode;

	public PullPipeline(PushCallableBuilder cb, int poolSize, ResultHandler rh, BlockingMode mode){
		this.cb = cb;
		this.poolSize = poolSize;
		this.rh = rh;
		this.mode = mode;
	}

	public void processRange() throws Exception{

		//TODO: create pools as we go and handle executor-related exceptions with proper finally blocks
		
		ExecutorService singlePullPool = Executors.newFixedThreadPool(this.poolSize);
		
		ConcurrentMap<Long, Future> results = new ConcurrentHashMap<>();

		Collection<SplitCallable> creatorsList = new ArrayList<>();

		// Don't need that many
		IntStream.rangeClosed(1, creatorPoolSize).forEach(i -> {
			creatorsList.add(cb.buildSplitCallable(executionPool, results));
		});

		splitterPool.invokeAll(creatorsList);
		splitterPool.shutdown();
		splitterPool.awaitTermination(30, TimeUnit.SECONDS); //TODO: get timeout from props

		results.values().stream().forEach(future -> {
			resultPool.submit(cb.buildHarvestCallable(rh, future));
		});

		executionPool.shutdown();
		if(this.mode.equals(BlockingMode.BLOCKING))
			executionPool.awaitTermination(30, TimeUnit.SECONDS);

		resultPool.shutdown();
		if(this.mode.equals(BlockingMode.BLOCKING))
			resultPool.awaitTermination(30, TimeUnit.SECONDS);
	}


}
