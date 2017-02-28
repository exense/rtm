package org.rtm.pipeline.split;

import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.builders.SEHCallableBuilder;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class SplitCallableImpl implements SplitCallable{

	private static final Logger logger = LoggerFactory.getLogger(SplitCallableImpl.class);
	
	private ExecutorService executor;
	private ConcurrentMap<Long, Future> results;
	private String id = ((Long)UUID.randomUUID().getMostSignificantBits()).toString();
	private OptimisticRangePartitioner<Long> orp;
	private SEHCallableBuilder taskBuilder;

	public SplitCallableImpl(ExecutorService executor, 
								ConcurrentMap<Long, Future> results,
								SEHCallableBuilder taskBuilder,
								OptimisticRangePartitioner<Long> orp){
		this.executor = executor;
		this.results = results;
		this.orp = orp;
		this.taskBuilder = taskBuilder;
	}

	@Override
	public Boolean call() throws Exception{
		while(this.orp.hasNext()){
			RangeBucket<Long> bucket = orp.next();
			if(bucket != null){ // due to optimistic hasNext
				Callable<LongRangeValue> task = taskBuilder.buildExecuteCallable(bucket);
				try {
					results.put(bucket.getLowerBound(),  executor.submit(task));
				} catch (Exception e) {
					logger.error(id + "; Failed to process task for bucket: " + bucket, e);
					throw e;
				}
			}
		}
		return true;
	}

}