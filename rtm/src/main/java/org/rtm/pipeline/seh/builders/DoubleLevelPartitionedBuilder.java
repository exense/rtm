package org.rtm.pipeline.seh.builders;

public class DoubleLevelPartitionedBuilder {}
/*
@SuppressWarnings("rawtypes")
public class DoubleLevelPartitionedBuilder extends SEHCallableBuilder{

	private OptimisticRangePartitioner orp;

	public DoubleLevelPartitionedBuilder(OptimisticRangePartitioner orp){
		this.orp = orp;
	}

	@Override
	public SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out, SEHCallableBuilder executeBuilder) {
		return new SplitCallableImpl(executor, results, taskBuilder, thisorp);
	}

	@Override
	public ExecuteCallable buildExecuteCallable() {}


	private Callable<LongRangeValue> buildTask(ExecutionLevel el, long intervalSize,
			List<Selector> sel, RangeBucket<Long> bucket, Properties requestProp)

	Callable<LongRangeValue> task = null;

	//case DOUBLE: //TODO: get ratio & threads from prop
		int parallelizationLevel = 3;
		long projected = Math.abs(intervalSize / parallelizationLevel);
		long subsize =  projected > 0?projected:1L;
		task = new SPEResplit(sel, bucket, requestProp, subsize, parallelizationLevel);
		//logger.debug("Built SubQueryTask for bucket="+bucket+"; with sub-interval size= " + subsize);
		break;
	

	return null;
}

@Override
public HarvestCallable buildHarvestCallable(ResultHandler rh, Future f) {
	// TODO Auto-generated method stub
	return null;
}
*/
