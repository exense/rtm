package org.rtm.requests;

import org.rtm.queries.TimebasedParallelExecutor;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamedSessionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamedSessionManager ssm;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamedSessionManager ssm){
		this.ssm = ssm;
	}

	@SuppressWarnings("rawtypes")
	public AbstractResponse handle(AggregationRequest aggReq){
		// extract info from request

		TimebasedParallelExecutor executor = new TimebasedParallelExecutor();

		AbstractResponse r = null;
		try {
			Stream streamHandle = executor.getResponseStream(aggReq.getSelectors(), aggReq.getLongTimeInterval(), aggReq.getProperties());
			r = new AggregationResponse(ssm.registerStreamSession(streamHandle));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getMessage());
		}
		return r;
	}

}
