package org.rtm.pipeline.builders.push;

import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.rtm.pipeline.callables.push.execute.ExecuteCallable;
import org.rtm.pipeline.callables.push.harvest.HarvestCallable;
import org.rtm.pipeline.callables.push.split.SplitCallable;
import org.rtm.range.RangeBucket;
import org.rtm.stream.result.ResultHandler;

@SuppressWarnings("rawtypes")
public abstract class PushCallableBuilder{

	public abstract SplitCallable buildSplitCallable(ExecutorService in, ConcurrentMap<Long, Future> out);
	public abstract ExecuteCallable buildExecuteCallable(RangeBucket<Long> bucket);
	public abstract HarvestCallable buildHarvestCallable(ResultHandler rh, Future f);
}
