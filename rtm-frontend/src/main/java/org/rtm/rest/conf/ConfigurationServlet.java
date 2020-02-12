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
package org.rtm.rest.conf;

import java.util.Map;
import java.util.TreeMap;

import javax.servlet.ServletContext;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.Configuration;

@Path("/configuration")
@SuppressWarnings({"unchecked", "rawtypes"})
public class ConfigurationServlet {

	private static String version = "3.0.0";

	@POST
	@Path("/getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationPost(@Context ServletContext sc) {return getConfiguration(sc);}

	@GET
	@Path("/getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationGet(@Context ServletContext sc) {return getConfiguration(sc);}

	public Response getConfiguration(ServletContext sc) {

		ConfigurationOutput response;
		try{
			response = new ConfigurationOutput();

			Map p = (Map)Configuration.getInstance().getUnderlyingPropertyObject();
			p.put("rtmVersion", version);
			response.setConfig(p);

			return Response.ok(response).build();
		}
		catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}

	@POST
	@Path("/getProperty")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPropertyPost(String propertyName) {
		return getProperty(propertyName);
	}

	@GET
	@Path("/getProperty")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getPeropertyGet(@QueryParam("propertyName") String propertyName) {
		return getProperty(propertyName);
	}

	public Response getProperty(String propertyName) {

		ConfigurationOutput response;
		try{
			response = new ConfigurationOutput();

			Map<String,String> p = new TreeMap<String,String>();
			p.put(propertyName, Configuration.getInstance().getProperty(propertyName));
			response.setConfig(p);

			return Response.ok(response).build();
		}
		catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}

	@GET
	@Path("/version")
	@Produces(MediaType.APPLICATION_JSON)
	public Response versionGet(@Context ServletContext sc) {return version(sc);}

	@POST
	@Path("/version")
	@Produces(MediaType.APPLICATION_JSON)
	public Response versionPost(@Context ServletContext sc) {return version(sc);}

	public Response version(ServletContext sc) {
		return Response.ok(version).build();
	}

}