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
package org.rtm.rest.worker;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.request.WorkerRequest;
import org.rtm.request.WorkerService;

/**
 * @author doriancransac
 *
 */
@Path("/worker")
public class WorkerServlet {

	//private static final Logger logger = LoggerFactory.getLogger(WorkerServlet.class);
	private WorkerService workerService = new WorkerService();

	@POST
	@Path("/work")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response workBucket(WorkerRequest req) throws Exception
	 {
		return Response.status(200).entity(
				workerService.produceValueForBucket(req.getSelectors(), req.getRangeBucket(), req.getProp())
				).build();
	}

}
