package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.query.ParallelRangeExecutor;
import org.rtm.query.ParallelRangeExecutor.ExecutionLevel;
import org.rtm.query.ParallelRangeExecutor.ExecutionType;
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

		int threadNb = 3;
		long timeout = 60L;
		
		try {
			LongTimeInterval effective = DBClient.figureEffectiveTimeBoundariesViaMongoDirect(lti, sel);
			//TODO: allow for custom interval size via prop
			logger.debug("effective: " + effective + "; span=" + effective.getSpan());
			long optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 20);
			logger.debug("optimal: " + optimalSize);
			

			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);
			ParallelRangeExecutor executor = new ParallelRangeExecutor("requestExecutor", effective, optimalSize,
					threadNb, timeout, rh, sel,
					ExecutionLevel.DOUBLE, ExecutionType.NON_BLOCKING,
					prop);
			
			//TODO: move to unblocking version
			executor.processRange();
			
			r = new AggregationResponse(ssm.registerStreamSession(stream));
			
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
