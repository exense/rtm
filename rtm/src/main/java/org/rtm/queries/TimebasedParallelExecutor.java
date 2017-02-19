package org.rtm.queries;
import java.util.List;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.buckets.OptimisticLongPartitioner;
import org.rtm.buckets.RangeBucket;
import org.rtm.core.LongTimeInterval;
import org.rtm.requests.guiselector.Selector;
import org.rtm.results.AggregationResult;
import org.rtm.results.ResultHandler;

public class TimebasedParallelExecutor {

	private OptimisticLongPartitioner olp;

	public TimebasedParallelExecutor(LongTimeInterval global, long bucketSize){
		olp = new OptimisticLongPartitioner(global.getBegin(), global.getEnd(), bucketSize);
	}

	public void processMongoQueryParallel(int nbThreads, long timeoutSecs, ResultHandler rm, List<Selector> sel, Properties requestProp) throws Exception{
		Vector<Callable<AggregationResult>> tasks = new Vector<>();
		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		IntStream.rangeClosed(1, nbThreads).forEach(
				i -> {
					if(olp.hasNext()){
						RangeBucket<Long> bucket = olp.next();
						if(bucket != null){ // due to optimistic hasNext
							tasks.addElement(
									new MongoSubqueryCallable(
											sel,
											bucket,
											requestProp));
						}
					}
				});

		for(Future<AggregationResult> f : executor.invokeAll(tasks, timeoutSecs, TimeUnit.SECONDS)){
			AggregationResult r = f.get(); 
			if(r != null){
				rm.attachResult(r);
			}
			else{
				throw new Exception("Null query result.");
			}
		}
	}

}

