package org.rtm.pipeline.execute.task;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.execute.callable.AbstractProduceMergeTask;
import org.rtm.pipeline.seh.SplitExecHarvestPipeline;
import org.rtm.pipeline.seh.SplitExecHarvestPipeline.BlockingMode;
import org.rtm.pipeline.seh.builders.SimpleMongoBuilder;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;
import org.rtm.stream.Stream;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;

public class MongoPartitionedQueryTask extends AbstractProduceMergeTask {

	protected List<Selector> sel;
	protected Properties prop;
	protected long partitioningFactor;
	private int poolSize;
	private Stream<Long> subResults;
	private ResultHandler<Long> resultHandler; 

	public MongoPartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize){
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
				prop);
				
		new SplitExecHarvestPipeline(
				builder,
				this.poolSize,
				this.resultHandler,
				BlockingMode.BLOCKING).processRange();
	}

	@Override
	@SuppressWarnings("unchecked")
	protected LongRangeValue merge(RangeBucket<Long> bucket) {
		LongRangeValue result = new LongRangeValue(bucket);
		subResults.values().stream().forEach(tv -> {
			tv.getData().values().stream().forEach(d -> {
				Dimension dim = (Dimension)d;
				String dimensionValue = dim.getDimensionValue();
				Dimension resDim = result.get(dimensionValue);
				if(resDim == null){
					resDim = new Dimension(dimensionValue);
					result.put(dimensionValue, resDim);
				}
				mergeMetricsForDimension(resDim, dim);
			});
		});
		return result;
	}

	private void mergeMetricsForDimension(Dimension resDim, Dimension dim) {
		dim.entrySet().stream().forEach(m -> {
			String metricName = (String) m.getKey();
			Long value = resDim.get(metricName);
			if(value == null){
				value = new Long(m.getValue().longValue());
				resDim.put(metricName, value);
			}
			else{
				long save = value.longValue();
				resDim.remove(metricName);
				resDim.put(metricName, save + m.getValue());
			}
		});
		
	}
}
