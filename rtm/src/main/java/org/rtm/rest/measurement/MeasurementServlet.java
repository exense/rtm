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

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.Configuration;
import org.rtm.commons.MeasurementConstants;
import org.rtm.request.AbstractResponse;
import org.rtm.request.AggregationRequest;
import org.rtm.request.ErrorResponse;
import org.rtm.request.MeasurementService;
import org.rtm.request.SuccessResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author doriancransac
 *
 */
@Path("/measurement")
public class MeasurementServlet {
	
	private static final Logger logger = LoggerFactory.getLogger(MeasurementServlet.class);
	private MeasurementService mserv = new MeasurementService();
	
	@POST
	@Path("/find")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response getResutStreamForQuery(final AggregationRequest body) {
		AbstractResponse response;
		try {
			int pager = Integer.parseInt(body.getServiceParams().getProperty("measurementService.nextFactor"));
			int pageSize = Configuration.getInstance().getPropertyAsInteger("client.MeasurementListView.pagingValue");
			
			response = new SuccessResponse(mserv.selectMeasurements(body.getSelectors1(), MeasurementConstants.BEGIN_KEY, pager * pageSize, pageSize), "ok");
		} catch (Exception e) {
			e.printStackTrace();
			response = new ErrorResponse("Error = " + e.getClass().getName() + " : " + e.getMessage());
		}
		return Response.status(200).entity(response).build();
	}

}