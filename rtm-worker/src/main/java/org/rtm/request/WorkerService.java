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

import org.rtm.db.BsonQuery;
import org.rtm.db.QueryClient;
import org.rtm.metrics.accumulation.MeasurementAccumulator;
import org.rtm.range.RangeBucket;
import org.rtm.selection.Selector;
import org.rtm.stream.LongRangeValue;

import com.fasterxml.jackson.databind.ObjectMapper;

import step.grid.agent.handler.AbstractMessageHandler;
import step.grid.agent.handler.context.OutputMessageBuilder;
import step.grid.agent.tokenpool.AgentTokenWrapper;
import step.grid.io.InputMessage;
import step.grid.io.OutputMessage;


/**
 * @author doriancransac
 *
 */
public class WorkerService extends AbstractMessageHandler{

	public LongRangeValue produceValueForBucket(List<Selector> selectors, RangeBucket<Long> rangeBucket, Properties prop) {

	 		LongRangeValue lrv = new LongRangeValue(rangeBucket.getLowerBound());
	 		QueryClient queryClient = new QueryClient(prop);
			BsonQuery query = queryClient.buildQuery(selectors, RangeBucket.toLongTimeInterval(rangeBucket));
			
			new MeasurementAccumulator(prop).handle(lrv, queryClient.executeQuery(query.getQuery()));
			return lrv;
	}

	/* (non-Javadoc)
	 * @see step.grid.agent.handler.MessageHandler#handle(step.grid.agent.tokenpool.AgentTokenWrapper, step.grid.io.InputMessage)
	 */
	@Override
	public OutputMessage handle(AgentTokenWrapper token, InputMessage message) throws Exception {
		ObjectMapper om = new ObjectMapper();
		WorkerRequest req = om.treeToValue(message.getPayload(), WorkerRequest.class);

		OutputMessageBuilder omb = new OutputMessageBuilder();
		LongRangeValue valueForBucket = produceValueForBucket(req.getSelectors(), req.getRangeBucket(), req.getProp());
		
		System.out.println("["+valueForBucket.getStreamPayloadIdentifier().getIdAsTypedObject()+"]="+valueForBucket);
		
		try {
			omb.setPayload(om.valueToTree(valueForBucket));
		} catch(Exception e) {
			e.printStackTrace();
			throw e;
		}
		
		return omb.build();
	}

}
