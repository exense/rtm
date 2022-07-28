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

import java.util.NoSuchElementException;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.rtm.measurement.MeasurementStatistics;
import org.rtm.metrics.postprocessing.MetricsManager;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ComparisonRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.rest.AbstractServlet;
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
@Singleton
@Tag(name = "RTM Aggregation")
@Hidden
public class AggregationServlet extends AbstractServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(AggregationServlet.class);

	private StreamBroker ssm;
	RequestHandler rh;

	@PostConstruct
	public void init() throws Exception {
		ssm = new StreamBroker(context.getConfiguration());
		rh = new RequestHandler(ssm, context.getConfiguration(), context.getMeasurementAccessor());
		this.context.setCleanupExecutorService(ssm.getExecutorService());
	}
	
	@POST
	@Path(AggregationConstants.getpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAggregationResutStream(final AggregationRequest body) {
		AbstractResponse rtmResponse = null;
		try{
		 rtmResponse = new SuccessResponse(rh.aggregate(body), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (NoSuchElementException e) {
			// silent
			logger.debug("A request finding no measurements has occured", e);
		} catch (Exception e) {
			String message = "A problem occured while retrieving stream with request= " + body; 
			logger.error(message, e);
			rtmResponse = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
			return Response.status(500).entity(rtmResponse).build();
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
			return Response.status(500).entity(rtmResponse).build();
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
				result = new MetricsManager(s.getStreamProp(),context.getConfiguration()).handle(s);
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
			return Response.status(500).entity(rtmResponse).build();
		}
		return Response.status(200).entity(rtmResponse).build();
	}
}