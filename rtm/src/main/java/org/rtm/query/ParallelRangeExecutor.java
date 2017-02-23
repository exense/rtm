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
import org.rtm.stream.Stream;
import org.rtm.stream.StreamResultHandler;
import org.rtm.stream.TimeValue;
import org.rtm.stream.ResultHandler;
import org.rtm.time.LongTimeInterval;
import org.rtm.time.OptimisticLongPartitioner;
import org.rtm.time.RangeBucket;

public class ParallelRangeExecutor {

	private OptimisticLongPartitioner olp;

	public Stream<Long> getResponseStream(List<Selector> sel, LongTimeInterval lti, Properties prop) throws Exception{
		Stream<Long> stream = new Stream<>();
		ResultHandler<Long> rh = new StreamResultHandler(stream);
		
		LongTimeInterval effective = QueryCallable.figureEffectiveTimeBoundariesViaMongoDirect(lti, sel);
		//TODO: allow for custom interval size via prop
		olp = new OptimisticLongPartitioner(effective.getBegin(), effective.getEnd(), QueryCallable.computeOptimalIntervalSize(effective.getSpan(), 20));
		
		processMongoQueryParallel(1, 60L, rh, sel, prop);
		
		return stream;
	}

	private void processMongoQueryParallel(int nbThreads, long timeoutSecs, ResultHandler<Long> rh, List<Selector> sel, Properties requestProp) throws Exception{
		ConcurrentMap<Long,Callable<TimeValue>> tasks = new ConcurrentHashMap<>();
		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		IntStream.rangeClosed(1, nbThreads).parallel().forEach(
				i -> {
					while(olp.hasNext()){
						RangeBucket<Long> bucket = olp.next();
						if(bucket != null){ // due to optimistic hasNext
							tasks.put(bucket.getIdAsTypedObject(),
									new QueryCallable(
											sel,
											bucket,
											requestProp
											));
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

