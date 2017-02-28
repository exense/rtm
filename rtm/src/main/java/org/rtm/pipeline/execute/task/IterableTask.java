package org.rtm.pipeline.execute.task;

import java.util.Map;

import org.rtm.range.RangeBucket;

@SuppressWarnings("rawtypes")
public interface IterableTask {

	Iterable<? extends Map> perform(RangeBucket<Long> bucket);

}
