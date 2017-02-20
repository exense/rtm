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

import org.rtm.core.LongTimeInterval;
import org.rtm.requests.guiselector.Selector;
import org.rtm.stream.TimeValue;
import org.rtm.stream.TimebasedResultHandler;
import org.rtm.time.OptimisticLongPartitioner;
import org.rtm.time.RangeBucket;

public class TimebasedParallelExecutor {

	private OptimisticLongPartitioner olp;

	public TimebasedParallelExecutor(LongTimeInterval global, long bucketSize){
		olp = new OptimisticLongPartitioner(global.getBegin(), global.getEnd(), bucketSize);
	}

	public void processMongoQueryParallel(int nbThreads, long timeoutSecs, TimebasedResultHandler<Long> rh, List<Selector> sel, Properties requestProp) throws Exception{
		Vector<Callable<TimeValue>> tasks = new Vector<>();
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
											requestProp
											));
						}
					}
				});

		for(Future<TimeValue> f : executor.invokeAll(tasks, timeoutSecs, TimeUnit.SECONDS)){
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

