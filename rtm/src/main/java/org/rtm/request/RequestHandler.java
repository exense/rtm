package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.pipeline.PushPipeline;
import org.rtm.pipeline.PushPipeline.BlockingMode;
import org.rtm.pipeline.builders.push.SubpartitionedQueryPushBuilder;
import org.rtm.range.time.LongTimeInterval;
import org.rtm.request.selection.Selector;
import org.rtm.stream.Stream;
import org.rtm.stream.StreamBroker;
import org.rtm.stream.result.ResultHandler;
import org.rtm.stream.result.StreamResultHandler;
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

		try {
			int poolSize = 3;
			int subPartitioning = 20;
			int subPoolSize = 20;
			
			LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
			long optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 20);
			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);
			
			logger.debug("effective=" + effective + "; optimalSize=" + optimalSize);		
			SubpartitionedQueryPushBuilder builder = new SubpartitionedQueryPushBuilder(
					effective.getBegin(),
					effective.getEnd(),
					optimalSize,
					sel,
					prop,
					subPartitioning,
					subPoolSize);
			
			/*
			SimpleMongoBuilder builder = new SimpleMongoBuilder(
					effective.getBegin(),
					effective.getEnd(),
					optimalSize,
					sel,
					new MergingAccumulator(prop));
			*/
			
			new PushPipeline(builder, poolSize, rh, BlockingMode.NON_BLOCKING).processRange();
			r = new AggregationResponse(ssm.registerStreamSession(stream));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
