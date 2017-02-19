package org.rtm.requests;

import org.rtm.orchestration.SplittingOrchestrator;
import org.rtm.stream.StreamedSessionManager;
import org.rtm.struct.Stream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamedSessionManager ssm;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamedSessionManager ssm){
		this.ssm = ssm;
	}

	@SuppressWarnings("rawtypes")
	public AbstractResponse handle(AggregationRequest aggregationRequest){
		// extract info from request

		SplittingOrchestrator mo = new SplittingOrchestrator(aggregationRequest.getDateInterval());

		AbstractResponse r = null;
		try {
			Stream streamHandle = mo.execute(aggregationRequest.getSelectors(), aggregationRequest.getProperties());
			r = new AggregationResponse(ssm.registerStreamSession(streamHandle));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getMessage());
		}
		return r;
	}

}
