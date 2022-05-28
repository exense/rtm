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
package org.rtm.rest.measurement;

import jakarta.annotation.PostConstruct;
import jakarta.inject.Singleton;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.StreamingOutput;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.MeasurementService;
import org.rtm.request.SuccessResponse;
import org.rtm.rest.AbstractServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */
@Singleton
@Path("/measurement")
public class MeasurementServlet extends AbstractServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(MeasurementServlet.class);
	private MeasurementService mserv;

	@PostConstruct
	public void init() throws Exception {
		mserv = new MeasurementService(context.getMeasurementAccessor());
	}
	
	@POST
	@Path("/find")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMeasurements(final AggregationRequest body) {
		AbstractResponse response;
		try {
			int pager = Integer.parseInt(body.getServiceParams().getProperty("measurementService.nextFactor"));
			int pageSize = context.getConfiguration().getPropertyAsInteger("client.MeasurementListView.pagingValue");
			
			response = new SuccessResponse(mserv.selectMeasurements(body.getSelectors1(), (String) body.getServiceParams().get("aggregateService.timeField"), 1, pager * pageSize, pageSize, body.getServiceParams()), "ok");
		} catch (Exception e) {
			e.printStackTrace();
			response = new ErrorResponse("Error = " + e.getClass().getName() + " : " + e.getMessage());
			return Response.status(500).entity(response).build();
		}
		return Response.status(200).entity(response).build();
	}
	
	@POST
	@Path("/latest")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getLatestMeasurements(final AggregationRequest body) {
		AbstractResponse response;
		try {
			int howmany = Integer.parseInt(body.getServiceParams().getProperty("measurementService.nextFactor"));

			response = new SuccessResponse(mserv.selectMeasurements(body.getSelectors1(), (String) body.getServiceParams().get("aggregateService.timeField"), -1, 0, howmany, body.getServiceParams()), "ok");
		} catch (Exception e) {
			e.printStackTrace();
			response = new ErrorResponse("Error = " + e.getClass().getName() + " : " + e.getMessage());
			return Response.status(500).entity(response).build();
		}
		return Response.status(200).entity(response).build();
	}

	@POST
	@Path("/export")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces(MediaType.APPLICATION_OCTET_STREAM)
	public Response exportForm(@FormParam ("value")  String bodyJson){
		AbstractResponse response;
		try {
			ObjectMapper objectMapper = new ObjectMapper();
			AggregationRequest ar = objectMapper.readValue(bodyJson, AggregationRequest.class);
			return export(ar);
		} catch (Exception e) {
			e.printStackTrace();
			response = new ErrorResponse("Error = " + e.getClass().getName() + " : " + e.getMessage());
			return Response.status(500).entity(response).build();
		}
	}

	@POST
	@Path("/export")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response export(final AggregationRequest body) {
		AbstractResponse response;
		try {
			StreamingOutput measurementsAsOutputStream;
			String filename = body.getServiceParams().getProperty("measurementService.filename","measurements.zip");
			int howmany = Integer.parseInt(body.getServiceParams().getProperty("measurementService.nextFactor"));
			measurementsAsOutputStream = mserv.getMeasurementsAsOutputStream(body.getSelectors1(), (String) body.getServiceParams().get("aggregateService.timeField"), -1, 0, howmany, body.getServiceParams());
			return Response.ok(measurementsAsOutputStream)
					.type("application/octet-stream")
					.header("Content-Disposition","attachment; filename=\""+ filename + "\"")
					.build();
		} catch (Exception e) {
			e.printStackTrace();
			response = new ErrorResponse("Error = " + e.getClass().getName() + " : " + e.getMessage());
			return Response.status(500).entity(response).build();
		}
	}

}