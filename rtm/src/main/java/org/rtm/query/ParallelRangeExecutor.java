package org.rtm.query;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.request.selection.Selector;
import org.rtm.stream.ResultHandler;
import org.rtm.stream.TimeValue;
import org.rtm.time.LongTimeInterval;
import org.rtm.time.OptimisticLongPartitioner;
import org.rtm.time.RangeBucket;

public class ParallelRangeExecutor {

	private OptimisticLongPartitioner olp;
	private long intervalSize;
	
	public ParallelRangeExecutor(LongTimeInterval effective, long intervalSize){
		this.intervalSize = intervalSize;
		olp = new OptimisticLongPartitioner(effective.getBegin(), effective.getEnd(), intervalSize);
	}

	public void processRangeSingleLevelBlocking(ResultHandler<Long> rh,
			List<Selector> sel, Properties prop,
			int threadNb, long timeout) throws Exception{

		//TODO: get threading & timeout values from prop
		executeQueryParallelBlocking(threadNb, timeout, "single", rh, sel, prop);
	}
	
	public void processRangeDoubleLevelBlocking(ResultHandler<Long> rh,
			List<Selector> sel, Properties prop,
			int threadNb, long timeout) throws Exception{
		//TODO: get threading & timeout values from prop
		executeQueryParallelBlocking(threadNb, timeout, "double", rh, sel, prop);
	}

	//TODO: implement non blocking version (handle results in a different threads and return val
	// this means a flag collection will need to be updated to share status / progress information
	private void executeQueryParallelBlocking(int nbThreads, long timeoutSecs, String depth,
			ResultHandler<Long> rh,
			List<Selector> sel, Properties requestProp) throws Exception{
		ConcurrentMap<Long,Callable<TimeValue>> tasks = new ConcurrentHashMap<>();
		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		IntStream.rangeClosed(1, nbThreads).parallel().forEach(
				i -> {
					while(olp.hasNext()){
						RangeBucket<Long> bucket = olp.next();
						if(bucket != null){ // due to optimistic hasNext
							switch(depth){
							case "single":
								tasks.put(bucket.getIdAsTypedObject(),
										new QueryCallable(
												sel,
												bucket,
												requestProp
												));
								break;
							case "double": //TODO: get ratio from prop
								tasks.put(bucket.getIdAsTypedObject(),
										new SubQueryCallable(
												sel,
												bucket,
												requestProp,
												intervalSize,
												Math.abs(intervalSize / 10)
												));
								break;
							}

						}
					}
				});

		for(Future<TimeValue> f : executor.invokeAll(tasks.values(), timeoutSecs, TimeUnit.SECONDS)){
			TimeValue tv = f.get(); 
			if(tv != null){
				rh.attachResult(tv);
			}
			else{
				throw new Exception("Null query result.");
			}
		}
	}

}

