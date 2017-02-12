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

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.commons.lang3.StringUtils;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.commons.MeasurementConstants;
import org.rtm.rest.ingestion.SimpleResponse.STATUS;

@Path("/ingest")
public class IngestionServlet {

	@GET
	@Path("/generic")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveGenericMeasurementGet(@QueryParam("measurement") String json) {
		return saveMeasurement(json);
	}

	@POST
	@Path("/generic")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveGenericMeasurementPost(@FormParam("measurement") String json) {
		return saveMeasurement(json);
	}


	@POST
	@Path("/structured")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementWithOptionalPost(@FormParam("eId") String eId,
												  @FormParam("time") String time,
												  @FormParam("name") String name,
												  @FormParam("value") String value,
												  @FormParam("optionalData") String optionalData) {
		
		return saveMeasurement(buildStructuredMeasurement(eId, time, name, value, optionalData));
	}
	
	@GET
	@Path("/structured/{eId}/{time}/{name}/{value}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementGet(@PathParam("eId") String eId,
												 @PathParam("time") String time,
												 @PathParam("name") String name,
												 @PathParam("value") String value) {
		
		return saveMeasurement(buildStructuredMeasurement(eId, time, name, value, null));
	}
	
	@GET
	@Path("/structured/{eId}/{time}/{name}/{value}/{optionalData}")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveStructuredMeasurementWithOptionalGet(@PathParam("eId") String eId,
												  @PathParam("time") String time,
												  @PathParam("name") String name,
												  @PathParam("value") String value,
												  @PathParam("optionalData") String optionalData) {
		
		return saveMeasurement(buildStructuredMeasurement(eId, time, name, value, optionalData));
	}
	
	public static Map<String, Object> buildStructuredMeasurement(String eId, String time, String name, String value, String optionalKeyValuePairs) {
		Map<String, Object> map = new HashMap<>();
		map.put(MeasurementConstants.EID_KEY, eId);
		map.put(MeasurementConstants.BEGIN_KEY, Long.parseLong(time));
		map.put(MeasurementConstants.NAME_KEY, name);
		map.put(MeasurementConstants.VALUE_KEY, value);
		
		if(optionalKeyValuePairs != null && !optionalKeyValuePairs.isEmpty())
			map.putAll(parseOptionalValues(optionalKeyValuePairs));
		
		return map;
	}

	public static Map<String, Object> parseOptionalValues(String optionalKeyValuePairs) {
		Map<String, Object> map = new HashMap<>();
		
		Matcher m = Pattern.compile("(.+?)=(.+?)(;|$)").matcher(optionalKeyValuePairs);
		
		while(m.find()){
			String s = m.group(2);
			if(StringUtils.isNumeric(s))
				map.put(m.group(1), Long.parseLong(s));
			else
				map.put(m.group(1), s);
		}
		return map;
	}

	private Response saveMeasurement(String json) {

		SimpleResponse resp = new SimpleResponse();

		try{
			MeasurementAccessor.getInstance().saveMeasurement(json);			
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
			MeasurementAccessor.getInstance().saveMeasurement(measurement);			
			resp.setStatus(STATUS.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
}