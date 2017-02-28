package org.rtm.pipeline.seh.builders;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.execute.callable.ExecuteCallable;
import org.rtm.pipeline.harvest.callable.HarvestCallable;
import org.rtm.pipeline.split.callable.SplitCallable;
import org.rtm.range.RangeBucket;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public abstract class SEHCallableBuilder{

	public abstract SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out);
	
	public abstract ExecuteCallable buildExecuteCallable(RangeBucket<Long> bucket);
	public abstract HarvestCallable buildHarvestCallable(ResultHandler rh, Future f);
}
