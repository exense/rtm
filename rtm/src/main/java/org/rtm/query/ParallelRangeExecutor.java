package org.rtm.query;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
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
		//TODO: get threading & timeout values from prop
		executeQueryParallelBlocking();
	}

	public void processRangeDoubleLevelBlocking() throws Exception{

		this.el = ExecutionLevel.DOUBLE;
		//TODO: get threading & timeout values from prop
		executeQueryParallelBlocking();
	}

	private void executeQueryParallelBlocking() throws Exception{

		ExecutorService taskCreators = Executors.newFixedThreadPool(nbThreads);
		ExecutorService rangeExecutors = Executors.newFixedThreadPool(nbThreads);

		List<RangeTaskCreator> creatorsList = new ArrayList<>();
		
		IntStream.rangeClosed(1, nbThreads).forEach(i -> {
			creatorsList.add(new RangeTaskCreator(rangeExecutors));
		});
		
		taskCreators.invokeAll(creatorsList);
		taskCreators.shutdown();
		taskCreators.awaitTermination(30, TimeUnit.SECONDS);

		rangeExecutors.shutdown();
		rangeExecutors.awaitTermination(this.timeoutSecs, TimeUnit.SECONDS);
		
		if(this.potentialException != null)
			throw this.potentialException;
	}

	private Callable<LongRangeValue> buildTask(List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp) {

		Callable<LongRangeValue> task = null;

		switch(this.el){
		case SINGLE:
			task = new QueryCallable(sel, bucket, requestProp);
			//logger.debug("Built QueryTask for bucket="+bucket+";");
			break;
		case DOUBLE: //TODO: get ratio from prop
			long subsize = Math.abs(this.intervalSize / 10);
			task = new SubQueryCallable(sel, bucket, requestProp, subsize);
			//logger.debug("Built SubQueryTask for bucket="+bucket+"; with sub-interval size= " + subsize);
			break;
		}

		return task;
	}
	
	private class RangeTaskCreator implements Callable<Boolean>{
		
		private ExecutorService executor;
		
		protected RangeTaskCreator(ExecutorService executor){
			this.executor = executor;
		}

		@Override
		public Boolean call() {
			//logger.debug("RangeTaskCreator executing.");
			while(olp.hasNext()){
				RangeBucket<Long> bucket = olp.next();
				if(bucket != null){ // due to optimistic hasNext

					Callable<LongRangeValue> task = buildTask(sel, bucket, requestProp);
					LongRangeValue lrv = null;
					try {
						lrv = executor.submit(task).get(timeoutSecs, TimeUnit.SECONDS);
						if(lrv != null)
							rh.attachResult(lrv);
						else
							throw new Exception("Null result.");
					} catch (Exception e) {
						logger.error(id + "; Failed to process task for bucket: " + bucket, e);
						potentialException = e;
					}
				}
			}
			return true;
			
		}
		
	}

}

