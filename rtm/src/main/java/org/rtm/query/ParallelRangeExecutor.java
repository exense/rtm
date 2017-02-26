package org.rtm.query;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.ResultHandler;
import org.rtm.time.LongTimeInterval;
import org.rtm.time.OptimisticLongPartitioner;
import org.rtm.time.RangeBucket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ParallelRangeExecutor {

	public enum ExecutionLevel{
		SINGLE, DOUBLE;
	}

	private static final Logger logger = LoggerFactory.getLogger(ParallelRangeExecutor.class);

	private OptimisticLongPartitioner olp;
	private long intervalSize;
	private ExecutionLevel el = null;
	private String id;
	private int nbThreads;
	private long timeoutSecs;
	private ResultHandler<Long> rh;
	private List<Selector> sel;
	private Properties requestProp;
	private Exception potentialException;

	public ParallelRangeExecutor(String id, LongTimeInterval effective, long intervalSize,
			int nbThreads, long timeoutSecs,
			ResultHandler<Long> rh, List<Selector> sel, Properties requestProp){
		this.id = id;
		this.nbThreads = nbThreads;
		this.timeoutSecs = timeoutSecs;
		this.rh = rh;
		this.sel = sel;
		this.requestProp = requestProp;
		this.intervalSize = intervalSize;
		olp = new OptimisticLongPartitioner(effective.getBegin(), effective.getEnd(), intervalSize);
	}

	public void processRangeSingleLevelBlocking() throws Exception{

		this.el = ExecutionLevel.SINGLE;
		executeQueryParallelBlocking();
	}

	public void processRangeDoubleLevelBlocking() throws Exception{

		this.el = ExecutionLevel.DOUBLE;
		executeQueryParallelBlocking();
	}

	private void executeQueryParallelBlocking() throws Exception{

		ExecutorService splitterPool = Executors.newFixedThreadPool(nbThreads);
		ExecutorService executionPool = Executors.newFixedThreadPool(nbThreads);
		ExecutorService resultPool = Executors.newFixedThreadPool(nbThreads);
		ConcurrentMap<Long, Future<LongRangeValue>> results = new ConcurrentHashMap<>();

		List<RangeTaskCreator> creatorsList = new ArrayList<>();

		IntStream.rangeClosed(1, nbThreads).forEach(i -> {
			creatorsList.add(new RangeTaskCreator(executionPool, results));
		});

		splitterPool.invokeAll(creatorsList);
		splitterPool.shutdown();
		splitterPool.awaitTermination(30, TimeUnit.SECONDS);

		results.values().stream().forEach(future -> {
			resultPool.submit(new ResultHandlerCallable(future));
		});

		executionPool.shutdown();
		executionPool.awaitTermination(30, TimeUnit.SECONDS);
		resultPool.shutdown();

		if(this.potentialException != null)
			throw this.potentialException;
	}

	private Callable<LongRangeValue> buildQueryTask(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp) {

		Callable<LongRangeValue> task = null;

		switch(this.el){
		case SINGLE:
			task = new QueryCallable(sel, bucket, requestProp);
			//logger.debug("Built QueryTask for bucket="+bucket+";");
			break;
		case DOUBLE: //TODO: get ratio & threads from prop
			int parallelizationLevel = 3;
			long projected = Math.abs(this.intervalSize / parallelizationLevel);
			long subsize =  projected > 0?projected:1L;
			task = new SubQueryCallable(sel, bucket, requestProp, subsize, parallelizationLevel);
			//logger.debug("Built SubQueryTask for bucket="+bucket+"; with sub-interval size= " + subsize);
			break;
		}

		return task;
	}

	private class RangeTaskCreator implements Callable<Boolean>{

		private ExecutorService executor;
		private ConcurrentMap<Long, Future<LongRangeValue>> results;
		private String id = ((Long)UUID.randomUUID().getMostSignificantBits()).toString();

		protected RangeTaskCreator(ExecutorService executor, ConcurrentMap<Long, Future<LongRangeValue>> results){
			this.executor = executor;
			this.results = results;
		}

		@Override
		public Boolean call() {
			//logger.debug("RangeTaskCreator executing.");
			int submissions = 0;
			while(olp.hasNext()){
				RangeBucket<Long> bucket = olp.next();
				if(bucket != null){ // due to optimistic hasNext

					Callable<LongRangeValue> task = buildQueryTask(sel, bucket, requestProp);
					try {
						results.put(bucket.getLowerBound(),  executor.submit(task));
					} catch (Exception e) {
						logger.error(id + "; Failed to process task for bucket: " + bucket, e);
						potentialException = e;
					}
				}
				submissions++;
			}

			//logger.debug("Task #" + this.id + " submitted " + submissions + " tasks.");
			return true;

		}

	}

	private class ResultHandlerCallable implements Callable<Boolean>{

		private Future<LongRangeValue> future;

		public ResultHandlerCallable(Future<LongRangeValue> future){
			this.future = future;
		}

		@Override
		public Boolean call() throws Exception {

			LongRangeValue lrv = future.get(timeoutSecs, TimeUnit.SECONDS);
			if(lrv != null)
				rh.attachResult(lrv);
			else
				throw new Exception("Null result.");

			return true;
		}

	}
}