package org.rtm.pipeline.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.measurement.AccumulationContext;
import org.rtm.measurement.SharingAccumulator;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.push.PushPipeline;
import org.rtm.pipeline.push.builders.PushQueryBuilder;
import org.rtm.pipeline.tasks.AbstractProduceMergeTask;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.Stream;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;

@SuppressWarnings("unused")
public abstract class PartitionedQueryTask extends AbstractProduceMergeTask{

		protected List<Selector> sel;
		protected Properties prop;
		protected long partitioningFactor;
		protected int poolSize;
		protected Stream<Long> subResults;
		protected ResultHandler<Long> resultHandler;
		protected SharingAccumulator accumulator;
		protected long subsize; 
		protected long timeoutSecs;

		public PartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize, long timeoutSecs){
			this.sel = sel;
			this.prop = prop;
			this.partitioningFactor = partitioningFactor;
			this.poolSize = poolSize;
			this.subResults = new Stream<>();
			this.resultHandler = new StreamResultHandler(subResults);
			this.timeoutSecs = timeoutSecs;
		}

		@Override
		protected void produce(RangeBucket<Long> bucket) throws Exception {

			this.accumulator = new SharingAccumulator(this.prop, bucket);
			
			long projected = Math.abs(bucket.getUpperBound() - bucket.getLowerBound() / this.partitioningFactor);
			this.subsize = projected > 0?projected:1L;
			
			executeParallel(bucket);
		}
		
		protected abstract void executeParallel(RangeBucket<Long> bucket)  throws Exception;

		@Override
		protected LongRangeValue merge(RangeBucket<Long> bucket) {
			AccumulationContext ac = this.accumulator.getAccumulationContext();
			ac.outerMerge();
			return ac;
		}
}
