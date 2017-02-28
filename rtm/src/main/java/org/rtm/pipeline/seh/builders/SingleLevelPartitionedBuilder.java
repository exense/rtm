package org.rtm.pipeline.seh.builders;

import java.util.Properties;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.execute.callable.ExecuteCallable;
import org.rtm.pipeline.execute.callable.RangeExecute;
import org.rtm.pipeline.execute.task.IterableTask;
import org.rtm.pipeline.harvest.callable.HarvestCallable;
import org.rtm.pipeline.harvest.callable.HarvestCallableImpl;
import org.rtm.pipeline.split.callable.SplitCallable;
import org.rtm.pipeline.split.callable.SplitCallableImpl;
import org.rtm.range.OptimisticLongPartitioner;
import org.rtm.range.OptimisticRangePartitioner;
import org.rtm.range.RangeBucket;
import org.rtm.stream.result.IterableMeasurementHandler;
import org.rtm.stream.result.MergingIterableResultHandler;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings({"rawtypes","unchecked"})
public abstract class SingleLevelPartitionedBuilder extends SEHCallableBuilder{

	private OptimisticRangePartitioner<Long> orp;
	private IterableMeasurementHandler handler;

	public SingleLevelPartitionedBuilder(Long start, Long end, Long increment, Properties prop){
		this.orp = new OptimisticLongPartitioner(start, end, increment);
		this.handler = new MergingIterableResultHandler(prop);
	}

	@Override
	public SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out) {
		return new SplitCallableImpl(in, out, this, this.orp);
	}
	
	@Override
	public ExecuteCallable buildExecuteCallable(RangeBucket<Long> bucket) {
		return new RangeExecute(bucket, createTask(), handler);
	}

	protected abstract IterableTask createTask();

	@Override
	public HarvestCallable buildHarvestCallable(ResultHandler rh, Future f) {
		return new HarvestCallableImpl(rh, f, 3); //TODO: get timeout from props
	}
}
