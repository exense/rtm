package org.rtm.pipeline.task;

import java.util.List;
import java.util.Properties;

import org.rtm.measurement.MergingAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.request.selection.Selector;
import org.rtm.stream.Dimension;
import org.rtm.stream.LongRangeValue;

public class MergingPartitionedQueryTask extends AbstractPartitionedQueryTask {

	public MergingPartitionedQueryTask(List<Selector> sel, Properties prop, long partitioningFactor, int poolSize) {
		super(sel, prop, partitioningFactor, poolSize);
		this.accumulator = new MergingAccumulator(this.prop);
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

	@Override
	protected void initWithBucket(RangeBucket<Long> bucket) {}
}
