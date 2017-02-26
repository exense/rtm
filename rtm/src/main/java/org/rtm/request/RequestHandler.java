package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.query.ParallelRangeExecutor;
import org.rtm.request.selection.Selector;
import org.rtm.stream.ResultHandler;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.StreamResultHandler;
import org.rtm.time.LongTimeInterval;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestHandler {

	private StreamBroker ssm;

	private static final Logger logger = LoggerFactory.getLogger(RequestHandler.class);

	public RequestHandler(StreamBroker ssm){
		this.ssm = ssm;
	}

	public AbstractResponse handle(AggregationRequest aggReq){
		
		List<Selector> sel = aggReq.getSelectors();
		LongTimeInterval lti = aggReq.getTimeWindow();
		Properties prop = aggReq.getProperties();

		AbstractResponse r = null;

		int threadNb = 10;
		long timeout = 10L;
		
		try {
			LongTimeInterval effective = DBClient.figureEffectiveTimeBoundariesViaMongoDirect(lti, sel);
			//TODO: allow for custom interval size via prop
			//logger.debug("effective: " + effective);
			long optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 30);
			//logger.debug("optimal: " + optimalSize);
			ParallelRangeExecutor executor = new ParallelRangeExecutor("requestExecutor", effective, optimalSize);
			
			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);
			
			//TODO: move to unblocking version
			executor.processRangeDoubleLevelBlocking(rh,
					sel, prop,
					threadNb, timeout);
			
			r = new AggregationResponse(ssm.registerStreamSession(stream));
			
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
