package org.rtm.pipeline.task;

import java.util.List;
import java.util.Properties;

import org.rtm.measurement.AccumulationContext;
import org.rtm.measurement.MeasurementAccumulator;
import org.rtm.measurement.SharingAccumulator;
import org.rtm.pipeline.SplitExecHarvestPipeline;
import org.rtm.pipeline.SplitExecHarvestPipeline.BlockingMode;
import org.rtm.pipeline.builders.SimpleMongoBuilder;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.Stream;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;

public abstract class AbstractPartitionedQueryTask extends AbstractProduceMergeTask{

		protected List<Selector> sel;
		protected Properties prop;
		protected long partitioningFactor;
		protected int poolSize;
		protected Stream<Long> subResults;
		private ResultHandler<Long> resultHandler;
		protected MeasurementAccumulator accumulator; 

		public AbstractPartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize){
			this.sel = sel;
			this.prop = prop;
			this.partitioningFactor = partitioningFactor;
			this.poolSize = poolSize;
			this.subResults = new Stream<>();
			this.resultHandler = new StreamResultHandler(subResults);
		}

		@Override
		protected void produce(RangeBucket<Long> bucket) throws Exception {

			SimpleMongoBuilder builder = new SimpleMongoBuilder(
					bucket.getLowerBound(),
					bucket.getUpperBound(),
					this.partitioningFactor,
					sel,
					new SharingAccumulator(this.prop, bucket));
					
			new SplitExecHarvestPipeline(
					builder,
					this.poolSize,
					this.resultHandler,
					BlockingMode.BLOCKING).processRange();
		}
}
