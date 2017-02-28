package org.rtm.pipeline.seh;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.pipeline.seh.builders.SEHCallableBuilder;
import org.rtm.pipeline.split.callable.SplitCallable;
import org.rtm.stream.result.ResultHandler;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes"})
public class SplitExecHarvestPipeline{

	public enum BlockingMode{
		BLOCKING, NON_BLOCKING;
	}
	
	//private static final Logger logger = LoggerFactory.getLogger(SplitExecHarvestPipeline.class);

	private SEHCallableBuilder cb;
	private int poolSize;
	private ResultHandler rh;
	private BlockingMode mode;

	public SplitExecHarvestPipeline(SEHCallableBuilder cb, int poolSize, ResultHandler rh, BlockingMode mode){
		this.cb = cb;
		this.poolSize = poolSize;
		this.rh = rh;
		this.mode = mode;
	}

	public void processRange() throws Exception{

		//TODO: create pools as we go and handle executor-related exceptions with proper finally blocks
		ExecutorService splitterPool = Executors.newFixedThreadPool(poolSize);
		ExecutorService executionPool = Executors.newFixedThreadPool(poolSize);
		ExecutorService resultPool = Executors.newFixedThreadPool(poolSize);
		ConcurrentMap<Long, Future> results = new ConcurrentHashMap<>();

		Collection<SplitCallable> creatorsList = new ArrayList<>();

		IntStream.rangeClosed(1, poolSize).forEach(i -> {
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
