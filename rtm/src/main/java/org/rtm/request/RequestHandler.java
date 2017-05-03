package org.rtm.request;

import java.util.List;
import java.util.Properties;
import java.util.concurrent.Executors;

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

		//TODO: expose to client
		try {
			//prop.put("histogram.nbPairs", Configuration.getInstance().getProperty("histogram.nbPairs"));
			//prop.put("histogram.approxMs", Configuration.getInstance().getProperty("histogram.approxMs"));
			prop.put("histogram.nbPairs", "20");
			prop.put("histogram.approxMs", "500");
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			int poolSize = 1;

			long timeout = Long.parseLong(prop.getProperty("aggregateService.timeout"));
			int subPartitioning = Integer.parseInt(prop.getProperty("aggregateService.partition"));
			int subPoolSize = Integer.parseInt(prop.getProperty("aggregateService.cpu"));

			LongTimeInterval effective = DBClient.findEffectiveBoundariesViaMongo(lti, sel);
			Long optimalSize = null;

			String hardInterval = prop.getProperty("aggregateService.granularity");
			if( (hardInterval != null) && (hardInterval.toLowerCase().trim().length() > 0) && (hardInterval.equals("auto")))
				optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 60);
			else
				optimalSize = Long.parseLong(hardInterval);

			Stream<Long> stream = new Stream<>();
			stream.setTimeoutDurationSecs(timeout);
			stream.getStreamProp().setProperty(Stream.INTERVAL_SIZE_KEY, optimalSize.toString());
			ResultHandler<Long> rh = new StreamResultHandler(stream);

			logger.debug("New Aggregation Request : TimeWindow=[effective=" + effective + "; optimalSize=" + optimalSize + "]; props=" + prop + "; selectors=" + aggReq.getSelectors() + "; streamId=" + stream.getId());

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

			Executors.newSingleThreadExecutor().submit(waiter);

			ssm.registerStreamSession(stream);
			r = new SuccessResponse(stream.getId(), "Stream initialized. Call the streaming service next to start retrieving data.");

		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
