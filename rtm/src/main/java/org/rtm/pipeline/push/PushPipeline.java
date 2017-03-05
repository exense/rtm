package org.rtm.pipeline.push;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.push.builders.PushCallableBuilder;
import org.rtm.pipeline.push.callables.split.SplitCallable;
import org.rtm.stream.result.ResultHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({"rawtypes", "unused"})
public class PushPipeline{
	
	private static final Logger logger = LoggerFactory.getLogger(PushPipeline.class);

	private PushCallableBuilder cb;
	private int poolSize;
	private ResultHandler rh;
	private BlockingMode mode;

	public PushPipeline(PushCallableBuilder cb, int poolSize, ResultHandler rh, BlockingMode mode){
		this.cb = cb;
		this.poolSize = poolSize;
		this.rh = rh;
		this.mode = mode;
	}

	public void execute() throws Exception{

		//TODO: create pools as we go and handle executor-related exceptions with proper finally blocks
		
		int creatorPoolSize = 2;
		// Don't need that many
		ExecutorService splitterPool = Executors.newFixedThreadPool(creatorPoolSize);
		
		// Need a lot
		ExecutorService executionPool = Executors.newFixedThreadPool(poolSize);
		
		// Actually we do need a lot
		ExecutorService resultPool = Executors.newFixedThreadPool(10);
		
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
