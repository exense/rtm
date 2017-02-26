package org.rtm.query;
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

	@SuppressWarnings("unused")
	private Exception potentialException = null;

	public ParallelRangeExecutor(String id, LongTimeInterval effective, long intervalSize){
		this.id = id;
		this.intervalSize = intervalSize;
		olp = new OptimisticLongPartitioner(effective.getBegin(), effective.getEnd(), intervalSize);
	}

	public void processRangeSingleLevelBlocking(ResultHandler<Long> rh,
			List<Selector> sel, Properties prop,
			int threadNb, long timeout) throws Exception{

		this.el = ExecutionLevel.SINGLE;
		//TODO: get threading & timeout values from prop
		executeQueryParallel(threadNb, timeout, rh, sel, prop);
	}

	public void processRangeDoubleLevelBlocking(ResultHandler<Long> rh,
			List<Selector> sel, Properties prop,
			int threadNb, long timeout) throws Exception{

		this.el = ExecutionLevel.DOUBLE;
		//TODO: get threading & timeout values from prop
		executeQueryParallel(threadNb, timeout, rh, sel, prop);
	}

	//TODO: implement non blocking version (handle results in a different threads and return val
	// this means a flag collection will need to be updated to share status / progress information
	private void executeQueryParallel(int nbThreads, long timeoutSecs,
			ResultHandler<Long> rh, List<Selector> sel, Properties requestProp) throws Exception{

		ExecutorService executor = Executors.newFixedThreadPool(nbThreads);
		IntStream.rangeClosed(1, nbThreads).forEach(
				i -> {
					System.out.println(this.id + "; Thread " + Thread.currentThread() + "; Iteration " + i);
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
								logger.error(this.id + "; Failed to process task for bucket: " + bucket, e);
								this.potentialException = e;
							}
						}
					}
				});

		executor.shutdown();
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

}

