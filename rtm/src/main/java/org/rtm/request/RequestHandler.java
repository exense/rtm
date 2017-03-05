package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.push.PushPipeline;
import org.rtm.pipeline.push.builders.PushCallableBuilder;
import org.rtm.pipeline.push.builders.SubpartitionedPushBuilder;
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
			int poolSize = 4;
			long timeout = 120;
			int subPartitioning = 1;
			int subPoolSize = 1;
			
			LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
			long optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 10);
			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);
			
			logger.debug("effective=" + effective + "; optimalSize=" + optimalSize);

			PushCallableBuilder builder = new SubpartitionedPushBuilder(
					effective.getBegin(),
					effective.getEnd(),
					optimalSize,
					sel,
					prop,
					subPartitioning,
					subPoolSize,
					timeout);

			new PushPipeline(
					builder,
					poolSize,
					rh,
					BlockingMode.NON_BLOCKING).execute();
						
/*			
			PullTaskBuilder tb = new PullQueryBuilder(sel, new MergingAccumulator(prop));
			PullPipelineBuilder ppb = new SimplePipelineBuilder(
					effective.getBegin(), effective.getEnd(),
					optimalSize, rh, tb);
	
			
			PullTaskBuilder tb = new PartitionedPullQueryBuilder(sel, prop, subPartitioning, subPoolSize, timeout);
			PullPipelineBuilder ppb = new SimplePipelineBuilder(
					effective.getBegin(), effective.getEnd(),
					optimalSize, rh, tb);
			
			PullPipeline pp = new PullPipeline(ppb, poolSize, timeout, BlockingMode.NON_BLOCKING);
				
			pp.execute();
*/
			r = new AggregationResponse(ssm.registerStreamSession(stream));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
