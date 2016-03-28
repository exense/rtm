package org.rtm.rest;

import java.util.List;
import java.util.Map;

import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.rtm.commons.Configuration;
import org.rtm.commons.Measurement;
import org.rtm.core.AggregationService;
import org.rtm.core.ComplexServiceResponse;
import org.rtm.core.ComplexServiceResponse.Status;
import org.rtm.core.MeasurementService;
import org.rtm.dao.RTMMongoClient;
import org.rtm.exception.NoDataException;
import org.rtm.exception.ValidationException;

@Path("/service")
public class ServiceServlet {

	public static final int PAGE_SIZE = Configuration.getInstance().getPropertyAsInteger("measurementService.paging");
	public static final int AGGREGATE_MAXM = Configuration.getInstance().getPropertyAsInteger("aggregateService.maxMeasurements");
	public static final String AGGREGATE_DOMAIN = Configuration.getInstance().getProperty("aggregateService.domain");
	public static final String MEASUREMENT_DOMAIN = Configuration.getInstance().getProperty("measurementService.domain");
	public static final String dSep = Configuration.getInstance().getProperty("domainSeparator");
	@POST
	@Path("/aggregate")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getAggregatesPost(ServiceInput input) {
		
		final String serviceDomain = AGGREGATE_DOMAIN;

		ServiceOutput response;
		int skip =0;
		/* The backend skip/limit strategy does not really apply here as the data can be handle properly through aggregation */
		/* But a limit should still be set later to prevent queries that will last hours on end */
		int limit = AGGREGATE_MAXM;
		/**/
		
		try{
			new AggregateValidator().validate(input);
		}catch (ValidationException e) {
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
		try{

			Iterable<Measurement> it = RTMMongoClient.getInstance().selectMeasurements(input.getSelectors(), skip, limit, Configuration.NUM_PREFIX +Configuration.SPLITTER+ Measurement.BEGIN_KEY);
			AggregationService as = new AggregationService();

			Map<String,String> serviceParams = input.getServiceParams();
			ComplexServiceResponse inconsistent = as.buildAggregatesForTimeInconsistent(
					serviceParams.get(serviceDomain + dSep +Measurement.SESSION_KEY), 
					it,
					Long.parseLong(serviceParams.get(serviceDomain + dSep +Configuration.GRANULARITY_KEY)),
					//Configuration.TEXT_PREFIX +Configuration.SPLITTER+serviceParams.get(serviceDomain + dSep +Configuration.GROUPBY_KEY),
					serviceParams.get(serviceDomain + dSep +Configuration.GROUPBY_KEY),
					Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.VALUE_KEY, Measurement.SESSION_KEY
					);

			ComplexServiceResponse consistent = AggregationService.makeDataConsistent(inconsistent, Measurement.SESSION_KEY, Measurement.BEGIN_KEY, Measurement.END_KEY, Measurement.NAME_KEY);

			response = AggregationService.convertForJson(consistent.getPayload());
			if(inconsistent.getReturnStatus() == Status.WARNING)
				response.setWarning(inconsistent.getMessage());

			return Response.ok(response).build();
		}catch(NoDataException e){
			response = new ServiceOutput();
			response.setShallowPayload();
			response.setWarning("No data found with the given criteria.");
			return Response.ok(response).build();
		}
		catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}
	
	/* <DEBUG> */
//	@POST
//	@Path("/aggregate")
//	@Produces(MediaType.APPLICATION_JSON)
//	public Response getAggregatesPost(String input) {
//
//		System.out.println(input);
//	return Response.status(200).entity("ok").build();
//	}
	/* </DEBUG> */
	
	
	
	@POST
	@Path("/measurement")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getMeasurementsPost(ServiceInput input) {
		
		final String serviceDomain = MEASUREMENT_DOMAIN;
		
		try {
			new MeasurementValidator().validate(input);
		} catch (ValidationException e1) {
			e1.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e1.getMessage()).build();
		}
		int skip;
		int limit = PAGE_SIZE;
		try{
			skip = Integer.parseInt(input.getServiceParams().get("skip"));
		}catch(NumberFormatException e){
			//e.printStackTrace();
			skip = 0;
		}
		try{
			List<Measurement> result = new MeasurementService().selectMeasurements(input.getSelectors(), Configuration.NUM_PREFIX +Configuration.SPLITTER+ Measurement.BEGIN_KEY, skip, limit);
			return Response.ok(result).build();
		}catch(Exception e){
			e.printStackTrace();
			return Response.status(500).entity("Exception occured : " + e.getMessage()).build();
		}
	}


	/* The following is for Debug purposes*/

	@POST
	@Path("/allmeasurements")
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
	@Path("/allmeasurements")
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