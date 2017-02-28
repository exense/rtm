package org.rtm.request;

import java.util.List;
import java.util.Properties;

import org.rtm.db.DBClient;
import org.rtm.measurement.MergingAccumulator;
import org.rtm.pipeline.SplitExecHarvestPipeline;
import org.rtm.pipeline.SplitExecHarvestPipeline.BlockingMode;
import org.rtm.pipeline.builders.MergingSubpartitionedMongoBuilder;
import org.rtm.pipeline.builders.SharingSubpartitionedMongoBuilder;
import org.rtm.pipeline.builders.SimpleMongoBuilder;
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
			LongTimeInterval effective = DBClient.figureEffectiveTimeBoundariesViaMongoDirect(lti, sel);
			long optimalSize = DBClient.computeOptimalIntervalSize(effective.getSpan(), 20);
			Stream<Long> stream = new Stream<>();
			ResultHandler<Long> rh = new StreamResultHandler(stream);

			SimpleMongoBuilder builder = new SimpleMongoBuilder(
					effective.getBegin(),
					effective.getEnd(),
					optimalSize,
					sel,
					new MergingAccumulator(prop));
			/*
			MergingSubpartitionedMongoBuilder builder = new MergingSubpartitionedMongoBuilder(
					effective.getBegin(),
					effective.getEnd(),
					optimalSize,
					sel,
					prop,
					2,
					1);*/

					
			new SplitExecHarvestPipeline(builder, 1, rh, BlockingMode.NON_BLOCKING).processRange();
			r = new AggregationResponse(ssm.registerStreamSession(stream));
		} catch (Exception e) {
			String message = "Request processing failed. "; 
			logger.error(message, e);
			r = new ErrorResponse(message + e.getClass() + "; " + e.getMessage());
		}
		return r;
	}

}
