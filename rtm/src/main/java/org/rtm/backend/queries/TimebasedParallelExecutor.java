package org.rtm.backend.queries;
import java.util.Properties;
import java.util.Vector;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.bson.Document;
import org.rtm.backend.results.AggregationResult;
import org.rtm.backend.results.ResultHandler;
import org.rtm.buckets.OptimisticLongPartitioner;
import org.rtm.core.LongTimeInterval;

public class TimebasedParallelExecutor {

	private OptimisticLongPartitioner olp;

	public TimebasedParallelExecutor(LongTimeInterval global, long bucketSize){
		olp = new OptimisticLongPartitioner(global.getBegin(), global.getEnd(), bucketSize);
	}

	public void processParallel(int nbThreads, long timeoutSecs, ResultHandler rm, Document timelessQuery, Properties requestProp) throws Exception{
		Vector<Callable<AggregationResult>> tasks = new Vector<>();
		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		IntStream.rangeClosed(1, nbThreads).forEach(
				i -> tasks.addElement(new MongoSubqueryCallable(new MongoQuery(timelessQuery), olp.next(),requestProp)));

		for(Future<AggregationResult> f : executor.invokeAll(tasks, timeoutSecs, TimeUnit.SECONDS)){
			AggregationResult r = f.get(); 
			if(r != null){
				rm.attachResult(r);
			}
			else{
				//throw new Exception("Null query result.");
				System.out.println("awesome!");
			}
		}
	}

}

