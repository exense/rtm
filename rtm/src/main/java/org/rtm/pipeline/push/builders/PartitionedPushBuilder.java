package org.rtm.pipeline.push.builders;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.push.callables.execute.ExecuteCallable;
import org.rtm.pipeline.push.callables.execute.RangeExecute;
import org.rtm.pipeline.push.callables.harvest.HarvestCallable;
import org.rtm.pipeline.push.callables.harvest.HarvestCallableImpl;
import org.rtm.pipeline.push.callables.split.SplitCallable;
import org.rtm.pipeline.push.callables.split.SplitCallableImpl;
import org.rtm.pipeline.tasks.RangeTask;
import org.rtm.range.OptimisticLongPartitioner;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.range.RangeBucket;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings({"rawtypes","unchecked"})
public abstract class PartitionedPushBuilder extends PushCallableBuilder{

	private OptimisticRangePartitioner<Long> orp;

	public PartitionedPushBuilder(Long start, Long end, Long increment){
		this.orp = new OptimisticLongPartitioner(start, end, increment);
	}

	@Override
	public SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out) {
		return new SplitCallableImpl(in, out, this, this.orp);
	}
	
	@Override
	public ExecuteCallable buildExecuteCallable(RangeBucket<Long> bucket) {
		return new RangeExecute(bucket, createTask());
	}

	protected abstract RangeTask createTask();

	@Override
	public HarvestCallable buildHarvestCallable(ResultHandler rh, Future f) {
		return new HarvestCallableImpl(rh, f, 30); //TODO: get timeout from props
	}
}