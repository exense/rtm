package org.rtm.pipeline.push.builders;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.push.callables.execute.ExecuteCallable;
import org.rtm.pipeline.push.callables.harvest.HarvestCallable;
import org.rtm.pipeline.push.callables.split.SplitCallable;
import org.rtm.range.RangeBucket;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public abstract class PushCallableBuilder{

	public abstract SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out);
	public abstract ExecuteCallable buildExecuteCallable(RangeBucket<Long> bucket);
	public abstract HarvestCallable buildHarvestCallable(ResultHandler rh, Future f);
}
