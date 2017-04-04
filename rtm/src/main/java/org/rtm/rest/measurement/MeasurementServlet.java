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

import java.util.List;
import java.util.Map;

import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.Configuration;
import org.rtm.commons.MeasurementConstants;
import org.rtm.core.MeasurementService;
import org.rtm.dao.RTMMongoClient;
import org.rtm.exception.ValidationException;
import org.rtm.rest.aggregation.AggInput;

@Singleton
@Path("/measurement")
public class MeasurementServlet {
	
	@POST
	@Path("/find")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMeasurementsPost(AggInput input) {
		Configuration conf = Configuration.getInstance();
		//final String serviceDomain = conf.getProperty("measurementService.domain");
		
		try {
			new MeasurementInputValidator().validate(input);
			RTMMongoClient.getInstance();
		} catch (ValidationException e1) {
			e1.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e1.getMessage()).build();
		}
		int skip;
		int limit = conf.getPropertyAsInteger("measurementService.paging");
		try{
			skip = Integer.parseInt(input.getServiceParams().get("skip"));
		}catch(NumberFormatException e){
			//e.printStackTrace();
			skip = 0;
		}
		try{
			List<Map<String,Object>> result = new MeasurementService().selectMeasurements(input.getSelectors(), MeasurementConstants.BEGIN_KEY, skip, limit);
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}

	/* The following is for Debug purposes*/

	@POST
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementsPost() {
		try{
			return Response.ok(new MeasurementService().listAllMeasurements()).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/all")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAllMeasurementsGet() {
		try{
			return Response.ok(new MeasurementService().listAllMeasurements()).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}
}