package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.pipeline.commons.BlockingMode;
import org.rtm.pipeline.pull.PullPipeline;
import org.rtm.pipeline.pull.builders.PullPipelineBuilder;
import org.rtm.pipeline.pull.builders.SimplePipelineBuilder;
import org.rtm.pipeline.pull.builders.tasks.PartitionedPullQueryBuilder;
import org.rtm.pipeline.pull.builders.tasks.PullTaskBuilder;
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
		Properties prop = aggReq.getServiceParams();
		AbstractResponse r = null;

		try {
			int poolSize = 1;
			long timeout = 120;
			int subPartitioning = 32;
			int subPoolSize = 2;
			
			LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
			Long optimalSize = null;
			
			String hardInterval = prop.getProperty("aggregateService.granularity");
			if( (hardInterval != null) && (hardInterval.toLowerCase().trim().length() > 0) && (hardInterval.equals("auto")))
				optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 20);
			else
				optimalSize = Long.parseLong(hardInterval);
			
			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);
			
			logger.debug("effective=" + effective + "; optimalSize=" + optimalSize);

			PullTaskBuilder tb = new PartitionedPullQueryBuilder(sel, prop, subPartitioning, subPoolSize, timeout);
			PullPipelineBuilder ppb = new SimplePipelineBuilder(
					effective.getBegin(), effective.getEnd(),
					optimalSize, rh, tb);

			PullPipeline pp = new PullPipeline(ppb, poolSize, timeout, BlockingMode.BLOCKING);

			Runnable waiter = new Runnable() {
				
				@Override
				public void run() {
					try {
						pp.execute();
					} catch (Exception e) {
						e.printStackTrace();
					}finally{
						stream.setComplete(true);
					}
				}
			};

			waiter.run();
			
			r = new SuccessResponse(ssm.registerStreamSession(stream), "Stream initialized. Call the streaming service next to start retrieving data.");
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
