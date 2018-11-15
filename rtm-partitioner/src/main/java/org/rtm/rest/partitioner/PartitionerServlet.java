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
package org.rtm.rest.partitioner;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.request.PartitionerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */
@Path("/partitioner")
public class PartitionerServlet {

	private static final Logger logger = LoggerFactory.getLogger(PartitionerServlet.class);
	private PartitionerService mserv = new PartitionerService();

	@GET
	@Path("/partition")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getSomething()
	 {
		return Response.status(200).entity("hello world").build();
	}

}
