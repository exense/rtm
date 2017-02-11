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
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.MeasurementAccessor;
import org.rtm.rest.ingestion.SimpleResponse.STATUS;

@Path("/ingest")
public class IngestionServlet {

	@GET
	@Path("/generic")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementGet(@QueryParam("measurement") String json) {
		return saveMeasurement(json);
	}

	@POST
	@Path("/generic")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementPost(@FormParam("measurement") String json) {
		return saveMeasurement(json);
	}

	// Does it make sense to build a map object and
	// bson-serialize it again to save in mongo?
	// Might as well just use the string API, right?

	@POST
	@Path("/save/default")
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveMeasurementPost(Map<String,Object> json) {
		return saveMeasurement(json);
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

	private Response saveMeasurement(Map<String,Object> m) {

		SimpleResponse resp = new SimpleResponse();

		try{
			MeasurementAccessor.getInstance().saveMeasurement(m);
			resp.setStatus(STATUS.SUCCESS);
		}catch(Exception e){
			e.printStackTrace();
			resp.setStatus(STATUS.FAILED);
			resp.setMessage(e.getMessage());
		}

		return Response.ok().entity(resp).build();
	}
}