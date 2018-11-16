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
package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.commons.PipelineExecutionHelper;
import org.rtm.pipeline.pull.PullPipelineExecutor;
import org.rtm.pipeline.pull.builders.pipeline.PullRunableBuilder;
import org.rtm.pipeline.pull.builders.pipeline.RunableBuilder;
import org.rtm.pipeline.pull.builders.task.PartitionedRangeTaskBuilder;
import org.rtm.pipeline.pull.builders.task.RangeTaskBuilder;
import org.rtm.selection.Selector;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;


/**
 * @author doriancransac
 *
 */
@SuppressWarnings({"unchecked", "rawtypes"})
public class WorkerService{
	
	private static StreamBroker streamBroker = new StreamBroker();

	public StreamId processBucket(List<Selector> sel, Properties prop, long subPartitioning, int subPoolSize, int timeoutSecs,
			long start, long end, long increment, long optimalSize) throws Exception{

		Stream<Long> stream = initStream(timeoutSecs, optimalSize, prop);
		ResultHandler<Long> rh = new StreamResultHandler(stream);
		
		/*Partitioning logic - how do we produce ranges*/
		RangeTaskBuilder tb = new PartitionedRangeTaskBuilder(sel, prop, subPartitioning, subPoolSize, timeoutSecs);
		
		/*Pull logic - produce runables which will do work while the partioner still has ranges*/
		RunableBuilder runableBuilder = new PullRunableBuilder(
				start, end, increment, rh, tb);

		// It's only useful to // at this level if we're looking to produce highly granular results,
		// which should almost never be the case
		PullPipelineExecutor pp = new PullPipelineExecutor(runableBuilder, /*poolSize*/ 1, timeoutSecs, BlockingMode.BLOCKING);

		PipelineExecutionHelper.executeAndsetListeners(pp, stream);

		streamBroker.registerStreamSession(stream);

		return stream.getId();
	}
	
	private Stream<Long> initStream(long timeout, Long optimalSize, Properties prop) {
		
		prop.setProperty(Stream.INTERVAL_SIZE_KEY, optimalSize.toString());
				
		Stream<Long> stream = new Stream<>(prop);
		stream.setTimeoutDurationSecs(timeout);

		return stream;
	}
}