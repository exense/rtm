package org.rtm.pipeline.task;

import java.util.List;
import java.util.Properties;

import org.rtm.measurement.AccumulationContext;
import org.rtm.measurement.SharingAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;

public class SharingPartitionedQueryTask extends AbstractPartitionedQueryTask {

	public SharingPartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize) {
		super(sel, prop, partitioningFactor, poolSize);
	}

	@Override
	protected LongRangeValue merge(RangeBucket<Long> bucket) {
		AccumulationContext ac = ((SharingAccumulator)this.accumulator).getAccumulationContext();
		ac.outerMerge();
		return ac;
	}

	@Override
	protected void initWithBucket(RangeBucket<Long> bucket) {
		System.out.println("Creating Accumulator for bucket: " + bucket);
		this.accumulator = new SharingAccumulator(this.prop, bucket);
	}

}
