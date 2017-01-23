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

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.Attributes;
import java.util.jar.Manifest;

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
import org.rtm.dao.RTMMongoClient;
import org.rtm.exception.ConfigurationException;
import org.rtm.exception.RTMException;

@Path("/configuration")
public class ConfigurationServlet {

	private static String version = null;

	@POST
	@Path("/reload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reloadPost(@Context ServletContext sc) {return reload(sc);}

	@GET
	@Path("/reload")
	@Produces(MediaType.APPLICATION_JSON)
	public Response reloadGet(@Context ServletContext sc) {return reload(sc);}

	public Response reload(ServletContext sc) {

		try {

			String hostname = Configuration.getInstance().getProperty(Configuration.DSHOST_KEY);
			String dbname = Configuration.getInstance().getProperty(Configuration.DSNAME_KEY);
			String measurementsColl = Configuration.getInstance().getProperty(Configuration.MEASUREMENTSCOLL_KEY);

			Configuration.triggerReload();

			if(Configuration.getInstance() == null)
				throw new RTMException("Configuration reload failed somehow.");
			else{
				String newHostname = Configuration.getInstance().getProperty(Configuration.DSHOST_KEY);
				String newDbname = Configuration.getInstance().getProperty(Configuration.DSNAME_KEY);
				String newMeasurementsColl = Configuration.getInstance().getProperty(Configuration.MEASUREMENTSCOLL_KEY);

				if(newHostname == null || newDbname == null || newMeasurementsColl == null)
					throw new RTMException("Reload failed due to null DB config values : host="+newHostname+", name="+newDbname+", measurementColl="+newMeasurementsColl);
				else{
					if(!hostname.equals(newHostname) || !dbname.equals(newDbname) || !measurementsColl.equals(newMeasurementsColl))
						RTMMongoClient.getInstance().triggerReload();
				}
			}
		} catch (ConfigurationException e) {
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		} catch (RTMException e) {
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
		return getConfiguration(sc); 
	}

	@POST
	@Path("/getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationPost(@Context ServletContext sc) {return getConfiguration(sc);}

	@GET
	@Path("/getConfiguration")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getConfigurationGet(@Context ServletContext sc) {return getConfiguration(sc);}

	public Response getConfiguration(ServletContext sc) {

		if(version == null || version.isEmpty())
		{
			try{
				initVersion(sc.getResourceAsStream("/META-INF/MANIFEST.MF"));
			}
			catch(Exception e){
				e.printStackTrace();
				return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
			}
		}

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

		if(version == null || version.isEmpty())
		{
			try{
				initVersion(sc.getResourceAsStream("/META-INF/MANIFEST.MF"));
			}
			catch(Exception e){
				e.printStackTrace();
				return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
			}
		}

		return Response.ok(version).build();
	}

	public synchronized void initVersion(InputStream is) throws IOException {
		/* No more war file, so this broke...
		//Manifest mf = new Manifest(new FileInputStream(sc.getRealPath("/META-INF/MANIFEST.MF")));
		Manifest mf = new Manifest(is);
		Attributes ats = mf.getMainAttributes();
		version = ats.getValue("Implementation-Version"); */

		// we need to go look into the pom instead of manifest since there's no more war file.
		version = "0.4.0";
	}
}