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
package org.rtm.rest.partitioner;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.measurement.MeasurementStatistics;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.request.AbstractResponse;
import org.rtm.request.ErrorResponse;
import org.rtm.request.PartitionerService;
import org.rtm.request.SuccessResponse;
import org.rtm.request.aggregation.StreamResponseWrapper;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */
@Path("/partitioner")
public class PartitionerServlet {

	private static final Logger logger = LoggerFactory.getLogger(PartitionerServlet.class);
	private static StreamBroker streamBroker = new StreamBroker();
	private PartitionerService partitionerService = new PartitionerService(streamBroker);

	@POST
	@Path("/partition")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response partitionBucket(PartitionerRequest req) throws Exception
	{
		StreamId streamId = partitionerService.processBucket(req.getSel(), req.getProp(), req.getSubPartitioning(),
				req.getSubPoolSize(), req.getTimeoutSecs(), req.getStart(), req.getEnd(), req.getIncrement(), req.getOptimalSize());

		return Response.status(200).entity(streamId).build();
	}

	@POST
	@Path("/read")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshResutStreamForId(StreamId streamId) {
		AbstractResponse rtmResponse = null;
		try {
			@SuppressWarnings("rawtypes")
			Stream s = this.streamBroker.getStreamAndFlagForRefresh(streamId);
			Stream result = null;
			if(s.isCompositeStream())
				result = s;
			else{
				result = new MetricsManager(s.getStreamProp()).handle(s);
				result.setComplete(s.isComplete());
			}
			StreamResponseWrapper wr = new StreamResponseWrapper(result, new MeasurementStatistics(s.getStreamProp()).getMetricList());
			//logger.debug(result.toString());
			rtmResponse = new SuccessResponse(wr,
					"Found stream with id=" + streamId + ". Delivering payload at time=" + System.currentTimeMillis());
		} catch (Exception e) {
			String message = "A problem occured while retrieving stream with request= " + streamId; 
			logger.error(message, e);
			rtmResponse = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return Response.status(200).entity(rtmResponse).build();
	}

}
