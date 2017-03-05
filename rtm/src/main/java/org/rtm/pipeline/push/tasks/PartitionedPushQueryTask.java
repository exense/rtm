package org.rtm.pipeline.push.tasks;

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

public class PartitionedPushQueryTask extends AbstractProduceMergeTask{

		protected List<Selector> sel;
		protected Properties prop;
		protected long partitioningFactor;
		protected int poolSize;
		protected Stream<Long> subResults;
		private ResultHandler<Long> resultHandler;
		protected SharingAccumulator accumulator; 

		public PartitionedPushQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize){
			this.sel = sel;
			this.prop = prop;
			this.partitioningFactor = partitioningFactor;
			this.poolSize = poolSize;
			this.subResults = new Stream<>();
			this.resultHandler = new StreamResultHandler(subResults);
		}

		@Override
		protected void produce(RangeBucket<Long> bucket) throws Exception {

			this.accumulator = new SharingAccumulator(this.prop, bucket);
			
			long projected = Math.abs(bucket.getUpperBound() - bucket.getLowerBound() / this.partitioningFactor);
			long subsize = projected > 0?projected:1L;
			
			PushQueryBuilder builder = new PushQueryBuilder(
					bucket.getLowerBound(),
					bucket.getUpperBound(),
					subsize,
					sel,
					this.accumulator);
					
			new PushPipeline(
					builder,
					this.poolSize,
					this.resultHandler,
					BlockingMode.BLOCKING).execute();
		}
		
		@Override
		protected LongRangeValue merge(RangeBucket<Long> bucket) {
			AccumulationContext ac = this.accumulator.getAccumulationContext();
			ac.outerMerge();
			return ac;
		}
}
