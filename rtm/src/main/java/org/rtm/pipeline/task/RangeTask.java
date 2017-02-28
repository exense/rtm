package org.rtm.pipeline.task;

import org.rtm.range.RangeBucket;
import org.rtm.stream.LongRangeValue;

public interface RangeTask {

	LongRangeValue perform(RangeBucket<Long> bucket) throws Exception;

}
