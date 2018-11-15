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
package org.rtm.rest.aggregation;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.measurement.MeasurementStatistics;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ComparisonRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */
@Path(AggregationConstants.servletPrefix)
public class AggregationServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(AggregationServlet.class);

	private static StreamBroker ssm = new StreamBroker();
	RequestHandler rh = new RequestHandler(ssm);
	
	@POST
	@Path(AggregationConstants.getpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAggregationResutStream(final AggregationRequest body) {
		AbstractResponse rtmResponse = null;
		try{
		 rtmResponse = new SuccessResponse(rh.aggregate(body), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (Exception e) {
			String message = "A problem occured while retrieving stream with request= " + body; 
			logger.error(message, e);
			rtmResponse = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
			return Response.status(200).entity(rtmResponse).build();
		
	}
	
	@POST
	@Path(AggregationConstants.comparepath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComparisonResutStream(final ComparisonRequest body) {
		AbstractResponse rtmResponse = null;
		try{
			rtmResponse = new SuccessResponse(rh.compare(body), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (Exception e) {
			String message = "A problem occured while retrieving stream with request= " + body; 
			logger.error(message, e);
			rtmResponse = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
			return Response.status(200).entity(rtmResponse).build();
		
	}
	
	@POST
	@Path(AggregationConstants.refreshpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshResutStreamForId(StreamId body) {
		AbstractResponse rtmResponse = null;
		try {
			@SuppressWarnings("rawtypes")
			Stream s = ssm.getStreamAndFlagForRefresh(body);
			Stream result = null;
			if(s.isCompositeStream())
				result = s;
			else{
				result = new MetricsManager(s.getStreamProp()).handle(s);
				result.setComplete(s.isComplete());
				}
			WrappedResult wr = new WrappedResult(result, new MeasurementStatistics(s.getStreamProp()).getMetricList());
			//logger.debug(result.toString());
			rtmResponse = new SuccessResponse(wr,
							"Found stream with id=" + body + ". Delivering payload at time=" + System.currentTimeMillis());
		} catch (Exception e) {
			String message = "A problem occured while retrieving stream with request= " + body; 
			logger.error(message, e);
			rtmResponse = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return Response.status(200).entity(rtmResponse).build();
	}
}