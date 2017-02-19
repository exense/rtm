package org.rtm.requests;

import org.rtm.orchestration.MongoOrchestrator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private ResponseSessionState rss;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(ResponseSessionState rss){

		// for right now : send back all results in a stateless way
		// eventually : implement stateful, incremental result handling & diffs/updates
		this.rss = rss;
	}

	public AbstractResponse handle(Request aggregationRequest){
		// extract info from request

		MongoOrchestrator mo = new MongoOrchestrator(aggregationRequest.getDateInterval());

		AbstractResponse r = null;
		try {
			r = new Response(mo.execute(aggregationRequest.getSelectors(), aggregationRequest.getProperties()), rss.getState());
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getMessage());
		}
		return r;
	}

}
