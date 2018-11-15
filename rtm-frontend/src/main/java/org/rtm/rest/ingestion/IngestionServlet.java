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

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.rest.ingestion.SimpleResponse.STATUS;
import org.rtm.utils.MeasurementUtils;

@Path(IngestionConstants.servletPrefix)
public class IngestionServlet {

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
		
		return saveMeasurement(MeasurementUtils.structuredToMap(eId, time, name, value, optionalData));
	}
	
	@GET
	@Path(IngestionConstants.structuredPrefix + "/{eId}/{time}/{name}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementGet(@PathParam(IngestionConstants.EID_KEY) String eId,
												 @PathParam(IngestionConstants.TIME_KEY) String time,
												 @PathParam(IngestionConstants.NAME_KEY) String name,
												 @PathParam(IngestionConstants.VALUE_KEY) String value) {
		
		return saveMeasurement(MeasurementUtils.structuredToMap(eId, time, name, value));
	}
	
	@GET
	@Path(IngestionConstants.structuredPrefix + "/{eId}/{time}/{name}/{value}/{optionalData}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementWithOptionalGet(@PathParam(IngestionConstants.EID_KEY) String eId,
												  @PathParam(IngestionConstants.TIME_KEY) String time,
												  @PathParam(IngestionConstants.NAME_KEY) String name,
												  @PathParam(IngestionConstants.VALUE_KEY) String value,
												  @PathParam(IngestionConstants.OPTIONALDATA_KEY) String optionalData) {
		
		return saveMeasurement(MeasurementUtils.structuredToMap(eId, time, name, value, optionalData));
	}
	
	private Response saveMeasurement(String json) {

		SimpleResponse resp = new SimpleResponse();

		try{
			MeasurementAccessor.getInstance().sendStructuredMeasurement(json);			
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
			MeasurementAccessor.getInstance().sendStructuredMeasurement(measurement);			
			resp.setStatus(STATUS.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
}