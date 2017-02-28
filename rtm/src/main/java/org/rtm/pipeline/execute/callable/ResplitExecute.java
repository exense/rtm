/*******************************************************************************
 * (C) Copyright 2016 Dorian Cransac and Jerome Comte
 *  
 * This file is part of rtm
 *  
 * rtm is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *  
 * rtm is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *  
 * You should have received a copy of the GNU Affero General Public License
 * along with rtm.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package org.rtm.pipeline.execute.callable;

public class ResplitExecute {}/*
@SuppressWarnings("unused")
public class ResplitExecute extends AbstractProduceMergeExecute {
	private static final Logger logger = LoggerFactory.getLogger(ResplitExecute.class);
	private long subRangeSize;
	
	private Stream<Long> subResults;
	private SplitExecHarvestPipeline pre;
	private UUID taskId;
	private Properties prop;
	private int parallelizationLevel;
	
	public ResplitExecute(List<Selector> sel, RangeBucket<Long> bucket,
			Properties requestProp, long subRangeSize, int parallelizationLevel) {
		super(sel, bucket, requestProp);
		this.taskId = UUID.randomUUID();
		this.subRangeSize = subRangeSize;
		this.prop = requestProp;
		this.parallelizationLevel = parallelizationLevel;
		this.subResults = new Stream<>();
		ResultHandler<Long> subHandler = new StreamResultHandler(subResults);
		//TODO: move to unblocking version, get nb threads & timeout from prop or constructor
		pre = new SplitExecHarvestPipeline("subQueryExecutor", RangeBucket.toLongTimeInterval(super.bucket), this.subRangeSize,
				this.parallelizationLevel, 60L, subHandler, super.sel,
				ExecutionLevel.SINGLE, BlockingMode.BLOCKING,
				requestProp);
	}
	
	@SuppressWarnings("unchecked")
	protected LongRangeValue merge() {
		LongRangeValue result = new LongRangeValue(super.bucket);
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
		// TODO: implement and use single point of accumulation (MeasurementHelper?)
		// merging by "generally" adding for right now
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

	protected void produce() throws Exception{
		//logger.debug("[" + this.taskId.toString() + "] Producing values now..");
		pre.processRange();
	}
	

}*/
