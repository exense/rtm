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
package org.rtm.rest.ingestion;

import java.util.Map;

import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.inject.Singleton;
import jakarta.ws.rs.FormParam;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

import org.rtm.rest.AbstractServlet;
import org.rtm.rest.ingestion.SimpleResponse.STATUS;

@Singleton
@Path(IngestionConstants.servletPrefix)
@Tag(name = "RTM Ingestion")
@Hidden
public class IngestionServlet extends AbstractServlet {

	@GET
	@Path(IngestionConstants.genericPrefix)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveGenericMeasurementGet(@QueryParam("measurement") String json) {
		return saveMeasurement(json);
	}

	@POST
	@Path(IngestionConstants.genericPrefix)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveGenericMeasurementPost(@FormParam("measurement") String json) {
		return saveMeasurement(json);
	}


	@POST
	@Path(IngestionConstants.structuredPrefix)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementWithOptionalPost(@FormParam(IngestionConstants.EID_KEY) String eId,
												  @FormParam(IngestionConstants.TIME_KEY) String time,
												  @FormParam(IngestionConstants.NAME_KEY) String name,
												  @FormParam(IngestionConstants.VALUE_KEY) String value,
												  @FormParam(IngestionConstants.OPTIONALDATA_KEY) String optionalData) {
		
		return saveMeasurement(context.getMeasurementUtils().structuredToMap(eId, time, name, value, optionalData));
	}
	
	@GET
	@Path(IngestionConstants.structuredPrefix + "/{eId}/{time}/{name}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementGet(@PathParam(IngestionConstants.EID_KEY) String eId,
												 @PathParam(IngestionConstants.TIME_KEY) String time,
												 @PathParam(IngestionConstants.NAME_KEY) String name,
												 @PathParam(IngestionConstants.VALUE_KEY) String value) {
		
		return saveMeasurement(context.getMeasurementUtils().structuredToMap(eId, time, name, value));
	}
	
	@GET
	@Path(IngestionConstants.structuredPrefix + "/{eId}/{time}/{name}/{value}/{optionalData}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementWithOptionalGet(@PathParam(IngestionConstants.EID_KEY) String eId,
												  @PathParam(IngestionConstants.TIME_KEY) String time,
												  @PathParam(IngestionConstants.NAME_KEY) String name,
												  @PathParam(IngestionConstants.VALUE_KEY) String value,
												  @PathParam(IngestionConstants.OPTIONALDATA_KEY) String optionalData) {
		
		return saveMeasurement(context.getMeasurementUtils().structuredToMap(eId, time, name, value, optionalData));
	}
	
	private Response saveMeasurement(String json) {

		SimpleResponse resp = new SimpleResponse();

		try{
			context.getMeasurementAccessor().sendStructuredMeasurement(json);			
			resp.setStatus(STATUS.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
	
	private Response saveMeasurement(Map<String, Object> measurement) {

		SimpleResponse resp = new SimpleResponse();

		try{
			context.getMeasurementAccessor().sendStructuredMeasurement(measurement);			
			resp.setStatus(STATUS.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
}