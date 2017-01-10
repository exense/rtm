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
package org.rtm.rest;

import java.nio.charset.Charset;

import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.Measurement;
import org.rtm.commons.MeasurementAccessor;
import org.rtm.rest.SimpleResponse.STATUS;

import com.fasterxml.jackson.databind.ObjectMapper;

@Path("/measurement")
public class MeasurementServlet {

	@GET
	@Path("/save/queryparam/string")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementGet(@QueryParam("measurement") String json) {
		System.out.println(json);
		return saveMeasurement(json);
	}
	
	@POST
	@Path("/save/formparam/string")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementPost(@FormParam("measurement") String json) {
		return saveMeasurement(json);
	}
	
	@GET
	@Path("/save/default")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementGet(@QueryParam("measurement") Measurement json) {
		return saveMeasurement(json);
	}
	
	@POST
	@Path("/save/default")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementPost(Measurement json) {
		return saveMeasurement(json);
	}
	
	private Response saveMeasurement(String json) {

		SimpleResponse resp = new SimpleResponse();

		try{
			ObjectMapper om = new ObjectMapper();
			Measurement t = om.readValue(json.getBytes(Charset.forName("UTF-8")), Measurement.class);
						
			MeasurementAccessor.getInstance().saveMeasurement(t);
			resp.setStatus(STATUS.SUCCESS);
//			if(acknowledged)
//			{
//			}else
//			{
//				resp.setStatus(STATUS.FAILED);
//				resp.setMessage(error);
//			}
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
	
	
	private Response saveMeasurement(Measurement t) {

		SimpleResponse resp = new SimpleResponse();

		try{
			MeasurementAccessor.getInstance().saveMeasurement(t);
			resp.setStatus(STATUS.SUCCESS);
			
			// The method getError disappeared from the API...
//			String error = MeasurementAccessor.getInstance().saveMeasurement(t).getError();
//			if(error == null || error.isEmpty())
//			{
//				resp.setStatus(STATUS.SUCCESS);
//			}else
//			{
//				resp.setStatus(STATUS.FAILED);
//				resp.setMessage(error);
//			}
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
}