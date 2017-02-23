package org.rtm.request;

import org.rtm.query.ParallelRangeExecutor;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamBroker ssm;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamBroker ssm){
		this.ssm = ssm;
	}

	@SuppressWarnings("rawtypes")
	public AbstractResponse handle(AggregationRequest aggReq){
		// extract info from request

		ParallelRangeExecutor executor = new ParallelRangeExecutor();

		AbstractResponse r = null;
		try {
			Stream streamHandle = executor.getResponseStream(aggReq.getSelectors(), aggReq.getTimeWindow(), aggReq.getProperties());
			r = new AggregationResponse(ssm.registerStreamSession(streamHandle));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getMessage());
		}
		return r;
	}

}
