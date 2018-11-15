package org.rtm.pipeline.commons.tasks;

import java.util.List;
import java.util.Properties;

import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.Stream;
import org.rtm.stream.WorkDimension;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;


public abstract class MergingPartitionedQueryTask extends AbstractProduceMergeTask{

	//private static final Logger logger = LoggerFactory.getLogger(MergingPartitionedQueryTask.class);

	protected List<Selector> sel;
	protected Properties prop;
	protected long partitioningFactor;
	protected int poolSize;
	protected Stream<Long> subResults;
	protected ResultHandler<Long> resultHandler;
	protected MeasurementAccumulator accumulator;
	protected long subsize; 
	protected long timeoutSecs;

	public MergingPartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize, long timeoutSecs){
		this.sel = sel;
		this.prop = prop;
		this.partitioningFactor = partitioningFactor;
		this.poolSize = poolSize;
		this.subResults = new Stream<>(prop);
		this.resultHandler = new StreamResultHandler(subResults);
		this.timeoutSecs = timeoutSecs;
		this.accumulator = new MeasurementAccumulator(prop);
	}

	@Override
	protected void produce(RangeBucket<Long> bucket) throws Exception {

		long projected = Math.abs((bucket.getUpperBound() - bucket.getLowerBound()) / this.partitioningFactor) +1;
		//logger.debug("projected= Math.abs(" + bucket.getUpperBound() + " - " + bucket.getLowerBound() + " / "+ this.partitioningFactor +" = " + projected);
		this.subsize = projected > 0?projected:1L;

		executeParallel(bucket);
	}

	protected abstract void executeParallel(RangeBucket<Long> bucket)  throws Exception;

	@Override
	@SuppressWarnings("unchecked")
	protected LongRangeValue merge(RangeBucket<Long> bucket) {
		LongRangeValue result = new LongRangeValue(bucket.getLowerBound());
		subResults.getStreamData().values().stream().forEach(tv -> {
			tv.getDimensionsMap().values().stream().forEach(d -> {
				WorkDimension dim = (WorkDimension)d;
				String dimensionValue = dim.getDimensionName();
				WorkDimension resDim = (WorkDimension)result.get(dimensionValue);
				if(resDim == null){
					resDim = new WorkDimension(dimensionValue);
					result.put(dimensionValue, resDim);
				}
				
				this.accumulator.mergeDimensionsLeft(resDim, dim);

			});
		});
		return result;
	}
}
