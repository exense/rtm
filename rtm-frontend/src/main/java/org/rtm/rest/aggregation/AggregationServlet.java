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
import java.util.HashMap;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;

import javax.annotation.PostConstruct;
import javax.inject.Singleton;
import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.RequestHandler;
import org.rtm.request.SuccessResponse;
import org.rtm.request.aggregation.StreamResponseWrapper;
import org.rtm.rest.partitioner.RefreshIdRequest;
import org.rtm.stream.StreamId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import step.grid.GridImpl;
import step.grid.TokenWrapper;
import step.grid.client.AbstractGridClientImpl.AgentCommunicationException;
import step.grid.client.GridClient;
import step.grid.client.GridClientException;
import step.grid.client.LocalGridClientImpl;
import step.grid.io.OutputMessage;

/**
 * @author doriancransac
 *
 */
@Singleton
@Path(AggregationConstants.servletPrefix)
public class AggregationServlet {

	private static final Logger logger = LoggerFactory.getLogger(AggregationServlet.class);
	private RequestHandler rh = new RequestHandler();
	
	private ConcurrentHashMap<String, TokenWrapper> tokenRegistry = new ConcurrentHashMap<>();

	//TODO: set up gracefully
	public static GridImpl partitionerGrid;
	public static GridImpl workerGrid;

	private GridClient partitionerClient;

	//TODO: maybe move the worker grid init somewhere else..
	@PostConstruct
	public void init() {
		try {
			this.partitionerClient = new LocalGridClientImpl(partitionerGrid);
		}catch(Exception e) {e.printStackTrace();}
	}

	@POST
	@Path(AggregationConstants.getpath)
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAggregationResutStream(final AggregationRequest request) {
		AbstractResponse rtmResponse = null;
		TokenWrapper tokenHandle = null;
		try{
			tokenHandle = partitionerClient.getTokenHandle(new HashMap<>(), new HashMap<>(), false);
		} catch (AgentCommunicationException e1) {	e1.printStackTrace();}

		try {
			StreamId id = rh.aggregate(request, this.partitionerClient, tokenHandle);
			rtmResponse = new SuccessResponse(id, "Stream initialized. Call the streaming service next to start retrieving data.");
			tokenRegistry.put(id.getStreamedSessionId(), tokenHandle);
		} catch (NoSuchElementException e) {
			rtmResponse = generateError(e, "No data matching selectors.", false);
		} catch (Exception e) {
			rtmResponse = generateError(e, request.toString(), true);
		}
		return Response.status(200).entity(rtmResponse).build();

	}

	//TODO: re-enable
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
		/*
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
			rtmResponse = generateError(e, streamId.toString(), true);
		}
		return Response.status(200).entity(rtmResponse).build();
		 */


		AbstractResponse rtmResponse = null;
		
		//TODO: Isolate, cache and factorize with RH code
		RefreshIdRequest req = new RefreshIdRequest();
		req.setStreamId(streamId);
		ObjectMapper om = new ObjectMapper();
		TokenWrapper tokenHandle = null;
		GridClient partitionerClient = null;
		partitionerClient = new LocalGridClientImpl(partitionerGrid);

		tokenHandle = tokenRegistry.get(streamId.getStreamedSessionId());

		OutputMessage message = null;
		try {
			message = partitionerClient.call(tokenHandle.getID(), om.valueToTree(req), "org.rtm.request.PartitionerService", null, new HashMap<>(), 300000);
		} catch (Exception e) {e.printStackTrace();}

		StreamResponseWrapper streamResult = om.treeToValue(message.getPayload(), StreamResponseWrapper.class);
		
		if(streamResult.getStream().isComplete()) {
			try {
				partitionerClient.returnTokenHandle(tokenHandle.getID());
				partitionerClient.close();
			} catch (AgentCommunicationException e) {
				e.printStackTrace();
			} catch (GridClientException e) {
				e.printStackTrace();
			}
		}
		
		rtmResponse = new SuccessResponse(streamResult,
				"Found stream with id=" + streamId + ". Delivering payload at time=" + System.currentTimeMillis());

		
		return Response.status(200).entity(rtmResponse).build();
	}

	private ErrorResponse generateError(Exception e, String details, boolean logError){
		if(logError)
			logger.error(details);
		System.err.println(details);
		e.printStackTrace();
		return new ErrorResponse(details +  "   (" + e.getClass().getName()+" : " +  (e.getMessage() == null?"no exception message":e.getMessage()) +  ")");
	}
}