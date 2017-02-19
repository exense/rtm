package org.rtm.requests;

import java.util.concurrent.ConcurrentMap;

import org.rtm.orchestration.SplittingOrchestrator;
import org.rtm.stream.StreamedSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamedSessionManager ssm;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamedSessionManager ssm){
		this.ssm = ssm;
	}

	public AbstractResponse handle(AggregationRequest aggregationRequest){
		// extract info from request

		SplittingOrchestrator mo = new SplittingOrchestrator(aggregationRequest.getDateInterval());

		AbstractResponse r = null;
		try {
			ConcurrentMap streamHandle = mo.execute(aggregationRequest.getSelectors(), aggregationRequest.getProperties());
			r = new AggregationResponse(ssm.registerStreamSession(streamHandle));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getMessage());
		}
		return r;
	}

}
