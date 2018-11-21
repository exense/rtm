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

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.client.HttpClient;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.stream.StreamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author doriancransac
 *
 */
@Path(AggregationConstants.servletPrefix)
public class AggregationServlet {

	private static final Logger logger = LoggerFactory.getLogger(AggregationServlet.class);

	RequestHandler rh = new RequestHandler();

	@POST
	@Path(AggregationConstants.getpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAggregationResutStream(final AggregationRequest request) {
		AbstractResponse rtmResponse = null;
		try{
			rtmResponse = new SuccessResponse(rh.aggregate(request), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (Exception e) {
			rtmResponse = generateError(e, request.toString());
		}
		return Response.status(200).entity(rtmResponse).build();

	}

	/*
	@POST
	@Path(AggregationConstants.comparepath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getComparisonResutStream(final ComparisonRequest body) {
		AbstractResponse rtmResponse = null;
		try{
			rtmResponse = new SuccessResponse(rh.compare(body), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (Exception e) {
			rtmResponse = generateError(e, body.toString());
		}
			return Response.status(200).entity(rtmResponse).build();

	}
	 */

	//Implement Forward

	@POST
	@Path(AggregationConstants.refreshpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response refreshResutStreamForId(StreamId streamId) throws JsonParseException, JsonMappingException, UnsupportedEncodingException, JsonProcessingException, IOException {
		AbstractResponse rtmResponse = null;
		try {
			HttpClient client = new HttpClient("localhost", 8098);
			ObjectMapper om = new ObjectMapper();
			String response = client.call(om.writeValueAsString(streamId), "/partitioner" ,"/read");

			// instanceof Success or Error Response..
			SuccessResponse partitionerResponse = om.readValue(response, SuccessResponse.class);

			client.close();
			rtmResponse = new SuccessResponse(partitionerResponse.getPayload(),
					"Found stream with id=" + streamId + ". Delivering payload at time=" + System.currentTimeMillis());
		} catch (Exception e) {
			rtmResponse = generateError(e, streamId.toString());
		}
		return Response.status(200).entity(rtmResponse).build();
	}

	private ErrorResponse generateError(Exception e, String details){
		String prefixedDetails = "\nRequest details= " + details; 
		logger.error(prefixedDetails, e);
		return new ErrorResponse(e.getMessage() + "   (" + e.getClass().getName() + ")");
	}
}