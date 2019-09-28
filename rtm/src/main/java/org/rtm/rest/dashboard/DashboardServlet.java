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
package org.rtm.rest.dashboard;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.bson.Document;
import org.rtm.commons.DBClient;
import org.rtm.commons.DashboardAccessor;
import org.rtm.commons.DashboardAccessor.DashboardCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * @author doriancransac
 *
 */
@Path("/visualization")
public class DashboardServlet {

	private static final Logger logger = LoggerFactory.getLogger(DashboardServlet.class);
	private static Map<String, Session> dmap = new HashMap<>();
	
	DashboardAccessor da = DashboardAccessor.getInstance();
	
	@POST
	@Path("/session")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response saveSession(final Session toSave) {
		dmap.put(toSave.getName(), toSave);
		ObjectMapper om = new ObjectMapper();
		//Map<String, Object> session = new HashMap<String, Object>();
		//session.put(toSave.getName(), om.valueToTree(toSave).toString());
		
		String sessionStr = "";
		try {
			sessionStr = om.writeValueAsString(toSave);
		} catch (JsonProcessingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		logger.info("asText= " + sessionStr);

		Document session = Document.parse(sessionStr);
		logger.info("sessionDoc= " + session);
		
		//session = Document.parse("{\"mydashboard\" : \"is awesome\"}");
		da.insertObject(session, DashboardCollection.SESSIONS);
		return Response.status(200).entity("{ \"status\" : \"ok\"}").build();
	}
	
	@GET
	@Path("/session")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Object loadSession(@QueryParam(value = "name") String name) {
		return Response.status(200).entity(da.getObject("mysession", DashboardCollection.SESSIONS)).build();
	}

}